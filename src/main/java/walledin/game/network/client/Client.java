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
import java.net.NetworkInterface;
import java.net.SocketAddress;
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
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkDataReader;
import walledin.game.network.NetworkDataWriter;
import walledin.game.network.NetworkEventListener;
import walledin.util.Utils;

public class Client implements RenderListener, NetworkEventListener, Runnable {
    private final static Logger LOG = Logger.getLogger(Client.class);
    private static final int PORT = 1234;
    private static final int TILE_SIZE = 64;
    private static final int TILES_PER_LINE = 16;

    private Font font;
    private final Renderer renderer; // current renderer
    private final EntityManager entityManager;
    private final SocketAddress host;
    private final String username;
    private final NetworkDataWriter networkDataWriter;
    private final NetworkDataReader networkDataReader;
    private final DatagramChannel channel;
    private String playerEntityName;
    private boolean quitting = false;
    private int receivedVersion = 0;

    public static void main(final String[] args) {
        final Renderer renderer = new Renderer();
        Client client;
        try {
            client = new Client(renderer);
        } catch (final IOException e) {
            LOG.fatal("IO exception while creation of client", e);
            return;
        }
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
     * @throws IOException
     */
    public Client(final Renderer renderer) throws IOException {
        this.renderer = renderer;
        entityManager = new EntityManager(new ClientEntityFactory());
        networkDataWriter = new NetworkDataWriter();
        networkDataReader = new NetworkDataReader(this);
        quitting = false;
        // Hardcode the host and username for now
        host = new InetSocketAddress("localhost", PORT);
        username = System.getProperty("user.name");
        channel = DatagramChannel.open();
    }

    /**
     * Run network code
     */
    @Override
    public void run() {
        try {
            doRun();
        } catch (final IOException e) {
            LOG.fatal("IOException during network loop", e);
        }
    }

    /**
     * Do the actual network loop.
     * 
     * @throws IOException
     */
    private void doRun() throws IOException {
        channel.configureBlocking(true);
        channel.connect(host);
        NetworkInterface networkInterface = NetworkInterface
                .getByInetAddress(channel.socket().getLocalAddress());
        LOG.debug("Connection MTU: " + networkInterface.getMTU());
        playerEntityName = NetworkConstants.getAddressRepresentation(channel
                .socket().getLocalSocketAddress());
        LOG.debug(playerEntityName);
        networkDataWriter.sendLoginMessage(channel, username);
        LOG.info("starting network loop");
        while (!quitting) {
            // Read messages. Locks on the entitymanager to prevent renderer or
            // update from being preformed half way
            networkDataReader.recieveMessage(channel, entityManager);
        }
        // write logout message
        networkDataWriter.sendLogoutMessage(channel);
    }

    /**
     * Called when the gamestate has been updated. We only send a new input when
     * we receive the net game state
     */
    @Override
    public boolean receivedGamestateMessage(final SocketAddress address,
            int oldVersion, int newVersion) {
        boolean result = false;
        if (LOG.isTraceEnabled()) {
            LOG.trace("version:" + newVersion + " receivedVersion:"
                    + receivedVersion);
        }
        if (receivedVersion == oldVersion && newVersion > receivedVersion) {
            receivedVersion = newVersion;
            result = true;
        }
        try {
            networkDataWriter.sendInputMessage(channel, receivedVersion, Input
                    .getInstance().getKeysDown());
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
        }
        return result;
    }

    @Override
    public void receivedLoginMessage(final SocketAddress address,
            final String name) {
        // ignore
    }

    @Override
    public void receivedLogoutMessage(final SocketAddress address) {
        // ignore
    }

    @Override
    public void receivedInputMessage(final SocketAddress address,
            int newVersion, final Set<Integer> keys) {
        // ignore
    }

    /**
     * Update the current game state
     * 
     * @param delta
     *            time since last update in seconds
     */
    @Override
    public void update(final double delta) {
        // prevent network from coming in between
        synchronized (entityManager) {
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
        }

        if (Input.getInstance().isKeyDown(KeyEvent.VK_ESCAPE)) {
            dispose();
            return;
        }

        /* Toggle full screen, current not working correctly */
        if (Input.getInstance().isKeyDown(KeyEvent.VK_F1)) {
            renderer.toggleFullScreen();
            Input.getInstance().setKeyUp(KeyEvent.VK_F1);
        }
    }

    /**
     * Render the current game state
     */
    @Override
    public void draw(final Renderer renderer) {
        // prevent network from coming in between
        synchronized (entityManager) {
            entityManager.draw(renderer); // draw all entities in correct order

            /* Render current FPS */
            renderer.startHUDRendering();
            font.renderText(renderer, "FPS: "
                    + Float.toString(renderer.getFPS()), new Vector2f(600, 20));
            renderer.stopHUDRendering();
        }
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

        font.readFromStream(Utils.getClasspathURL("arial20.font"));

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
        manager.loadFromURL(Utils.getClasspathURL("tiles.png"), "tiles");
        manager.loadFromURL(Utils.getClasspathURL("zon.png"), "sun");
        manager.loadFromURL(Utils.getClasspathURL("player.png"), "player");
        manager.loadFromURL(Utils.getClasspathURL("wall.png"), "wall");
        manager.loadFromURL(Utils.getClasspathURL("game.png"), "game");
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

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();
        }
    }
}
