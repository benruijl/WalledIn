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
package walledin.game.network.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.network.NetworkDataManager;
import walledin.game.network.NetworkEventListener;
import walledin.util.Utils;

/**
 * This class provides the server for the game. All gamestate updates happen
 * here. Clients can register to this class to be added to the game.
 * 
 */
public class Server implements NetworkEventListener {
	private static final Logger LOG = Logger.getLogger(Server.class);
	private static final int PORT = 1234;
	private static final int UPDATES_PER_SECOND = 30;
	private final Map<SocketAddress, PlayerConnection> players;
	private final Set<SocketAddress> newPlayers;
	private boolean running;
	private final NetworkDataManager networkManager;
	private Entity map;
	private long currentTime;
	private final EntityManager entityManager;

	/**
	 * Creates a new server. Initializes variables to their default values.
	 */
	public Server() {
		players = new HashMap<SocketAddress, PlayerConnection>();
		running = false;
		networkManager = new NetworkDataManager(this);
		newPlayers = new HashSet<SocketAddress>();
		entityManager = new EntityManager(new ServerEntityFactory());
	}

	/**
	 * Start of application. It runs the server.
	 * 
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 */
	public static void main(final String[] args) {
		try {
			new Server().run();
		} catch (final IOException e) {
			LOG.fatal("IOException during network loop", e);
		}
	}

	/**
	 * Runs the server. It starts a server channel and enters the main loop.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		LOG.info("initializing");
		init();
		final DatagramChannel channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(PORT));
		channel.configureBlocking(false);

		running = true;
		LOG.info("starting main loop");
		while (running) {
			final long time = System.nanoTime();
			doLoop(channel);
			double delta = System.nanoTime() - time;
			// convert to sec
			delta /= 1000000000;
			// Calculate the how many milliseconds are left
			final long left = (long) ((1d / UPDATES_PER_SECOND - delta) * 1000);
			try {
				if (left > 0) {
					Thread.sleep(left);
				}
			} catch (final InterruptedException e) {
				// TODO do something
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main loop of the server. Takes care of reading messages, updating
	 * gamestate, and sending messages.
	 * 
	 * @param channel
	 *            The channel to read from / send to
	 * @throws IOException
	 */
	private void doLoop(final DatagramChannel channel) throws IOException {
		// Clear the new players from the last loop
		newPlayers.clear();
		entityManager.clearChanges();
		// Read input messages and login messages
		boolean hasMore = networkManager.recieveMessage(channel, entityManager);
		while (hasMore) {
			hasMore = networkManager.recieveMessage(channel, entityManager);
		}

		double delta = System.nanoTime() - currentTime;
		currentTime = System.nanoTime();
		// convert to sec
		delta /= 1000000000;

		// Update each player, do connection checks
		for (final PlayerConnection p : players.values()) {
			if (p.update()) {
				networkManager.sendAliveMessage(channel, p.getAddress());
			}

		}

		// Update game state
		update(delta);
		// Write to all the clients
		sendGamestate(channel);
	}

	/**
	 * Writes updated game information to both new and current players. The new
	 * players receive extra data.
	 * 
	 * @param channel
	 *            The channel to send to
	 * @throws IOException
	 */
	private void sendGamestate(final DatagramChannel channel)
			throws IOException {
		if (!newPlayers.isEmpty()) {
			networkManager.prepareGamestateMessageNewPlayers(entityManager);
			for (final SocketAddress socketAddress : newPlayers) {
				networkManager.sendCurrentMessage(channel, socketAddress);
			}
		}
		if (!players.isEmpty()) {
			networkManager
					.prepareGamestateMessageExistingPlayers(entityManager);
			for (final SocketAddress socketAddress : players.keySet()) {
				if (!newPlayers.contains(socketAddress)) {
					networkManager.sendCurrentMessage(channel, socketAddress);
				}
			}
		}
	}

	/**
	 * Process alive message for player
	 */
	@Override
	public void receivedAliveMessage(final SocketAddress address) {
		players.get(address).processAliveReceived();
	}

	@Override
	public void receivedGamestateMessage(final SocketAddress address) {
		// ignore .. should not happen
	}

	/**
	 * Creates a connection to a new client.
	 * 
	 * @param name
	 *            Player name
	 * @param address
	 *            Player socket address
	 */
	@Override
	public void receivedLoginMessage(final SocketAddress address,
			final String name) {
		final String entityName = networkManager
				.getAddressRepresentation(address);
		final Entity player = entityManager.create("Player", entityName);
		newPlayers.add(address);
		player.setAttribute(Attribute.POSITION, new Vector2f(400, 300));
		player.setAttribute(Attribute.PLAYER_NAME, name);

		/* Let the player start with a handgun */
		final Entity weapon = entityManager.create("Handgun", entityManager.generateUniqueName("Handgun"));
		player.setAttribute(Attribute.WEAPON, weapon);

		final PlayerConnection con = new PlayerConnection(address, player);
		players.put(address, con);

		LOG.info("new player " + name + " @ " + address);
	}

	/**
	 * Log the player out
	 */
	@Override
	public void receivedLogoutMessage(final SocketAddress address) {
		LOG.info("Player " + address.toString() + " left the game.");
		newPlayers.remove(address);
		players.get(address).remove();
	}

	/**
	 * Set the new input state
	 */
	@Override
	public void receivedInputMessage(final SocketAddress address,
			final Set<Integer> keys) {
		final Entity player = players.get(address).getPlayer();
		if (player != null) {
			player.setAttribute(Attribute.KEYS_DOWN, keys);
		}
	}

	/**
	 * Update the gamestate, removes disconnected players and does collision
	 * detection.
	 * 
	 * @param delta
	 *            Time elapsed since last update
	 */
	public void update(final double delta) {
		/* Check if players left the game, and if so remove them */
		final List<SocketAddress> remList = new ArrayList<SocketAddress>();
		for (final PlayerConnection con : players.values()) {
			if (con.getAlive() == false) {
				entityManager.remove(con.getPlayer().getName());
				remList.add(con.getAddress());
			}
		}

		for (final SocketAddress sok : remList) {
			players.remove(sok);
		}

		/* Update all entities */
		entityManager.update(delta);

		/* Do collision detection */
		entityManager.doCollisionDetection(map, delta);
	}

	/**
	 * Initializes the game. It reads the default map and initializes the entity
	 * manager.
	 */
	public void init() {
		// initialize entity manager
		entityManager.init();

		final GameMapIO mapIO = new GameMapIOXML(entityManager); // choose XML
		// as format
		map = mapIO.readFromURL(Utils.getClasspathURL("map.xml"));
	}
}