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
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.network.NetworkDataManager;

public class Server {
	private static final int PORT = 1234;
	private static final int BUFFER_SIZE = 1024 * 1024;
	private static final int UPDATES_PER_SECOND = 60;
	private Map<SocketAddress, Entity> players;
	private Set<SocketAddress> newPlayers;
	private boolean running;
	private ByteBuffer buffer;
	private NetworkDataManager networkManager;
	private Entity map;
	private long currentTime;
	private final EntityManager entityManager;

	public Server() {
		players = new HashMap<SocketAddress, Entity>();
		running = false;
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		networkManager = new NetworkDataManager();
		newPlayers = new HashSet<SocketAddress>();
		entityManager = new EntityManager(new ServerEntityFactory());
	}

	public static void main(String[] args) throws IOException {
		new Server().run();
	}

	/**
	 * Run the server.
	 * @throws IOException
	 */
	public void run() throws IOException {
		init();
		DatagramChannel channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(PORT));
		channel.configureBlocking(false);

		running = true;
		while (running) {
			long time = System.nanoTime();
			doLoop(channel);
			double delta = System.nanoTime() - time;
			// convert to sec
			delta /= 1000000000;
			// Calculate the how many milliseconds are left
			long left = (long) ((1d / UPDATES_PER_SECOND - delta) * 1000);
			//System.out.println(left + " " + delta);
			try {
				if (left > 0) {
					Thread.sleep(left);
				}
			} catch (InterruptedException e) {
				// TODO do something
				e.printStackTrace();
			}
		}
	}

	private void doLoop(DatagramChannel channel) throws IOException {
		// Clear the new players from the last loop
		newPlayers.clear();
		// Read input messages and login messages
		readDatagrams(channel);
		double delta = System.nanoTime() - currentTime;
		currentTime = System.nanoTime();
		// convert to sec
		delta /= 1000000000;
		// Update game state
		update(delta);
		// Write to all the clients
		writeDatagrams(channel);
	}

	private void writeDatagrams(DatagramChannel channel) throws IOException {
		if (!newPlayers.isEmpty()) {
			writeDatagramForNewPlayers();
			buffer.flip();
			for (SocketAddress socketAddress : newPlayers) {
				channel.send(buffer, socketAddress);
				buffer.rewind();
			}
		}
		if (!players.isEmpty()) {
			writeDatagramForExistingPlayers();
			buffer.flip();
			for (SocketAddress socketAddress : players.keySet()) {
				if (!newPlayers.contains(socketAddress)) {
					channel.send(buffer, socketAddress);
					buffer.rewind();
				}
			}
		}
	}

	private void writeDatagramForExistingPlayers() {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.GAMESTATE_MESSAGE);
		Set<Entity> removedEntities = entityManager.getRemoved();
		Set<Entity> createdEntities = entityManager.getCreated();
		Collection<Entity> entities = entityManager.getAll();
		buffer.putInt(entities.size() + removedEntities.size());
		for (Entity entity : entities) {
			if (createdEntities.contains(entity)) {
				networkManager.writeCreateEntity(entity, buffer);
			} else {
				networkManager.writeEntity(entity, buffer);
			}
		}
		for (Entity entity : removedEntities) {
			networkManager.writeRemoveEntity(entity, buffer);
		}
	}

	private void writeDatagramForNewPlayers() {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.GAMESTATE_MESSAGE);
		Collection<Entity> entities = entityManager.getAll();
		buffer.putInt(entities.size());
		for (Entity entity : entities) {
			networkManager.writeCreateEntity(entity, buffer);
		}
	}

	private void readDatagrams(DatagramChannel channel) throws IOException {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		SocketAddress address = channel.receive(buffer);
		buffer.flip();
		while (address != null) {
			processDatagram(address);
			address = channel.receive(buffer);
			buffer.flip();
		}
	}

	private void processDatagram(SocketAddress address) {
		int ident = buffer.getInt();
		if (ident == NetworkDataManager.DATAGRAM_IDENTIFICATION) {
			byte type = buffer.get();
			switch (type) {
			case NetworkDataManager.LOGIN_MESSAGE:
				int nameLength = buffer.getInt();
				byte[] nameBytes = new byte[nameLength];
				buffer.get(nameBytes);
				String name = new String(nameBytes);
				createPlayer(name, address);
				break;
			case NetworkDataManager.LOGOUT_MESSAGE:
				removePlayer(address);
				break;
			case NetworkDataManager.INPUT_MESSAGE:
				short numKeys = buffer.getShort();
				Set<Integer> keys = new HashSet<Integer>();
				for (int i = 0; i < numKeys; i++) {
					keys.add((int) buffer.getShort());
				}
				Entity player = players.get(address);
				if (player != null) {
					player.setAttribute(Attribute.KEYS_DOWN, keys);
				}
				break;
			}
		}
	}

	private void removePlayer(SocketAddress address) {
		newPlayers.remove(address);
	}

	private void createPlayer(String name, SocketAddress address) {
		String entityName = networkManager.getAddressRepresentation(address);
		Entity player = entityManager.create("Player", entityName);
		newPlayers.add(address);
		player.setAttribute(Attribute.POSITION,
				new Vector2f(400, 300));
		player.setAttribute(Attribute.PLAYER_NAME, name);
		players.put(address, player);
		System.out.println("new player " + name + " @ " + address);
	}
	
	public void update(final double delta) {
		/* Update all entities */
		entityManager.update(delta);
		
		/* Do collision detection */
		entityManager.doCollisionDetection(map, delta);
	}

	/**
	 * Initialize game
	 */
	public void init() {
		// initialize entity manager
		entityManager.init();

		final GameMapIO mapIO = new GameMapIOXML(entityManager); // choose XML as format
		map = mapIO.readFromFile("data/map.xml");
	}
}
