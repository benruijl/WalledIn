/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.game.network.client;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.network.NetworkDataManager;

public class Client implements RenderListener, Runnable {
	private final static Logger LOG = Logger.getLogger(Client.class);
	private static final int PORT = 1234;
	private static final int BUFFER_SIZE = 1024 * 1024;
	private static final int TILE_SIZE = 64;
	private static final int TILES_PER_LINE = 16;
	private boolean running;
	private final ByteBuffer buffer;
	private Font font;
	private final Renderer renderer; // current renderer
	private final EntityManager entityManager;
	private final SocketAddress host;
	private final String username;
	private final NetworkDataManager networkManager;
	private String playerEntityName;

	public static void main(final String[] args) {
		final Renderer renderer = new Renderer();
		final Client client = new Client(renderer);
		LOG.info("initializing renderer");
		renderer.initialize("WalledIn", 800, 600, false);
		renderer.addListener(client);
		// Start renderer
		LOG.info("starting renderer");
		renderer.beginLoop();
	}

	/**
	 * Create the client
	 * 
	 * @param renderer
	 *            Current renderer
	 */
	public Client(final Renderer renderer) {
		this.renderer = renderer;
		entityManager = new EntityManager(new ClientEntityFactory());
		networkManager = new NetworkDataManager();
		running = false;
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		// Hardcode the host and username for now
		host = new InetSocketAddress("localhost", PORT);
		username = System.getProperty("user.name");;
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void doRun() throws IOException {
		final DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(true);
		channel.connect(host);
		playerEntityName = networkManager.getAddressRepresentation(channel
				.socket().getLocalSocketAddress());
		writeLogin(channel);
		running = true;
		LOG.info("starting network loop");
		while (running) {
			// Read gamestate
			readDatagrams(channel);
			// Write input
			writeInput(channel);
		}
	}

	private void writeInput(final DatagramChannel channel) throws IOException {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.INPUT_MESSAGE);
		final Set<Integer> keysDown = Input.getInstance().getKeysDown();
		buffer.putShort((short) keysDown.size());
		for (final int key : keysDown) {
			buffer.putShort((short) key);
		}
		buffer.flip();
		channel.write(buffer);
	}

	private void readDatagrams(final DatagramChannel channel)
			throws IOException {
		int ident = -1;
		while (ident != NetworkDataManager.DATAGRAM_IDENTIFICATION) {
			buffer.limit(BUFFER_SIZE);
			buffer.rewind();
			channel.read(buffer);
			buffer.flip();
			ident = buffer.getInt();
		}
		
		final byte type = buffer.get();

		switch (type) {
		// server asks if client is still alive. We reply with the same message
		// to confirm
		case NetworkDataManager.ALIVE_MESSAGE:
			ByteBuffer buf = ByteBuffer.allocate(6);
			buf.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
			buf.put(NetworkDataManager.ALIVE_MESSAGE);
			buf.flip();
			channel.write(buf);
			break;
		case NetworkDataManager.GAMESTATE_MESSAGE:
			processGamestate();
			break;
		default:
			LOG.warn("Received unhandled message");
			break;
		}

	}

	private void processGamestate() throws IOException {
		final int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			networkManager.readEntity(entityManager, buffer);
		}
	}

	private void writeLogin(final DatagramChannel channel) throws IOException {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.LOGIN_MESSAGE);
		buffer.putInt(username.length());
		buffer.put(username.getBytes());
		buffer.flip();
		channel.write(buffer);
	}

	@Override
	public void update(final double delta) {
		/* Update all entities */
		entityManager.update(delta);

		/* Center the camera around the player */
		if (playerEntityName != null) {
			final Entity player = entityManager.get(playerEntityName);
			if (player != null) {
				renderer.centerAround((Vector2f) player
						.getAttribute(Attribute.POSITION));
			}
		}

		/* Toggle full screen, current not working correctly */
		if (Input.getInstance().isKeyDown(KeyEvent.VK_F1)) {
			renderer.toggleFullScreen();
			Input.getInstance().setKeyUp(KeyEvent.VK_F1);
		}
	}

	@Override
	public void draw(final Renderer renderer) {
		entityManager.draw(renderer); // draw all entities in correct order

		/* Render current FPS */
		renderer.startHUDRendering();
		font.renderText(renderer, "FPS: " + Float.toString(renderer.getFPS()),
				new Vector2f(600, 20));
		renderer.stopHUDRendering();
	}

	/**
	 * Initialize game
	 */
	@Override
	public void init() {
		LOG.info("initializing client");
		loadTextures();
		createTextureParts();

		font = new Font(); // load font
		font.readFromFile("data/arial20.font");

		// initialize entity manager
		entityManager.init();

		// Background is not created by server (not yet anyway)
		entityManager.create("Background", "Background");

		LOG.info("starting network thread");
		// start network thread
		final Thread thread = new Thread(this, "network");
		thread.start();
	}

	private void loadTextures() {
		final TextureManager manager = TextureManager.getInstance();
		manager.loadFromFile("data/tiles.png", "tiles");
		manager.loadFromFile("data/zon.png", "sun");
		manager.loadFromFile("data/player.png", "player");
		manager.loadFromFile("data/wall.png", "wall");
		manager.loadFromFile("data/game.png", "game");
	}

	private void createTextureParts() {
		final TexturePartManager manager = TexturePartManager.getInstance();
		manager.createTexturePart("player_eyes", "player", new Rectangle(70,
				96, 20, 32));
		manager.createTexturePart("player_background", "player", new Rectangle(
				96, 0, 96, 96));
		manager.createTexturePart("player_body", "player", new Rectangle(0, 0,
				96, 96));
		manager.createTexturePart("player_background_foot", "player",
				new Rectangle(192, 64, 96, 32));
		manager.createTexturePart("player_foot", "player", new Rectangle(192,
				32, 96, 32));
		manager.createTexturePart("sun", "sun", new Rectangle(0, 0, 128, 128));
		manager.createTexturePart("tile_empty", "tiles",
				createMapTextureRectangle(6, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_filled", "tiles",
				createMapTextureRectangle(1, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass_end_left", "tiles",
				createMapTextureRectangle(4, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass_end_right", "tiles",
				createMapTextureRectangle(5, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass", "tiles",
				createMapTextureRectangle(16, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_left_grass", "tiles",
				createMapTextureRectangle(19, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_left_mud", "tiles",
				createMapTextureRectangle(20, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_right_mud", "tiles",
				createMapTextureRectangle(21, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_left_grass", "tiles",
				createMapTextureRectangle(32, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_left_mud", "tiles",
				createMapTextureRectangle(36, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_right_mud", "tiles",
				createMapTextureRectangle(37, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_left_grass_end", "tiles",
				createMapTextureRectangle(48, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_mud", "tiles",
				createMapTextureRectangle(52, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
	}

	private Rectangle createMapTextureRectangle(final int tileNumber,
			final int tileNumPerLine, final int tileWidth, final int tileHeight) {
		return new Rectangle((tileNumber % 16 * tileWidth + 1), (tileNumber
				/ 16 * tileHeight + 1), (tileWidth - 2), (tileHeight - 2));
	}
}
