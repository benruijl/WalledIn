package walledin.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import walledin.game.CollisionManager;
import walledin.game.EntityFactory;
import walledin.game.EntityManager;
import walledin.game.ItemFactory;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.math.Vector2f;

public class Server {
	private static final int PORT = 1234;
	private static final int BUFFER_SIZE = 1024 * 1024;
	private static final int UPDATES_PER_SECOND = 60;
	private static final int NANOSECONDS_PER_SECOND = 1000000000;
	private Map<SocketAddress, Entity> players;
	private Map<Entity, Set<Integer>> keysDown;
	private Map<String, Entity> entities;
	private Set<SocketAddress> newPlayers;
	private Set<Entity> removedEntities;
	private Set<Entity> newEntities;
	private boolean running;
	private ByteBuffer buffer;
	private NetworkManager networkManager;
	private Entity gameMap;
	private long currentTime;
	private final EntityManager entityManager;

	public Server() {
		entities = new LinkedHashMap<String, Entity>();
		players = new HashMap<SocketAddress, Entity>();
		running = false;
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		networkManager = new NetworkManager();
		newEntities = new HashSet<Entity>();
		removedEntities = new HashSet<Entity>();
		newPlayers = new HashSet<SocketAddress>();
		entityManager = new EntityManager(new EntityFactory());
	}

	public static void main(String[] args) throws IOException {
		new Server().run();
	}

	public void run() throws IOException {
		init();
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress(PORT));
		channel.configureBlocking(false);

		running = true;
		while (running) {
			long time = System.nanoTime();
			doLoop(channel);
			double delta = System.nanoTime() - time;
			// convert to sec
			delta /= 1000000000;
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
		removedEntities.clear();
		newEntities.clear();
		newPlayers.clear();
		readDatagrams(channel);
		double delta = System.nanoTime() - currentTime;
		currentTime = System.nanoTime();
		// convert to sec
		delta /= 1000000000;
		update(delta);
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
				if (newPlayers.contains(socketAddress)) {
					channel.send(buffer, socketAddress);
					buffer.rewind();
				}
			}
		}
	}

	private void writeDatagramForExistingPlayers() {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkManager.GAMESTATE_MESSAGE);
		buffer.putInt(entities.size() + removedEntities.size());
		for (Entity entity : entities.values()) {
			if (newEntities.contains(entity)) {
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
		buffer.putInt(NetworkManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkManager.GAMESTATE_MESSAGE);
		buffer.putInt(entities.size());
		for (Entity entity : entities.values()) {
			networkManager.writeCreateEntity(entity, buffer);
		}
	}

	private void readDatagrams(DatagramChannel channel) throws IOException {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		SocketAddress address = channel.receive(buffer);
		while (address != null) {
			processDatagram(address);
			address = channel.receive(buffer);
		}
	}

	private void processDatagram(SocketAddress address) {
		int ident = buffer.getInt();
		if (ident == NetworkManager.DATAGRAM_IDENTIFICATION) {
			byte type = buffer.get();
			switch (type) {
			case NetworkManager.LOGIN_MESSAGE:
				int nameLength = buffer.getInt();
				byte[] nameBytes = new byte[nameLength];
				buffer.get(nameBytes);
				String name = new String(nameBytes);
				createPlayer(name, address);
				break;
			case NetworkManager.LOGOUT_MESSAGE:
				removePlayer(address);
			case NetworkManager.INPUT_MESSAGE:
				short numKeys = buffer.getShort();
				Set<Integer> keys = new HashSet<Integer>();
				for (int i = 0; i < numKeys; i++) {
					keys.add((int) buffer.getShort());
				}
				Entity player = players.get(address);
				keysDown.put(player, keys);
				break;
			}
		}
	}

	private void removePlayer(SocketAddress address) {
		Entity player = players.get(address);
		newPlayers.remove(player);
		removeEntity(player.getName());
	}

	private void createPlayer(String name, SocketAddress address) {
		Entity player = entityManager.create("Player", name);
		newEntity(player);
		newPlayers.add(address);
		player.setAttribute(Attribute.POSITION,
				new Vector2f(400, 300));
		players.put(address, player);
	}

	private void newEntity(Entity entity) {
		entities.put(entity.getName(), entity);
		newEntities.add(entity);
	}

	public void update(final double delta) {
		/* Update all entities */
		for (final Entity entity : entities.values()) {
			entity.sendUpdate(delta);
		}

		/* Do collision detection */
		CollisionManager.calculateMapCollisions(gameMap, entities.values(),
				delta);
		CollisionManager.calculateEntityCollisions(entities.values(), delta);

		for (final Entity entity : entities.values()) {
			if (entity.isMarkedRemoved()) {
				removeEntity(entity.getName());
			}
		}
	}

	/**
	 * Initialize game
	 */
	public void init() {
		entities = new LinkedHashMap<String, Entity>();

		// load all item information
		ItemFactory.getInstance().loadFromXML("data/items.xml");

		final GameMapIO mapIO = new GameMapIOXML(entityManager); // choose XML as format

		gameMap = mapIO.readFromFile("data/map.xml");
		newEntity(gameMap);

		// add map items like healthkits to entity list
		final List<Entity> mapItems = gameMap.getAttribute(Attribute.ITEM_LIST);
		for (final Entity item : mapItems) {
			newEntity(item);
		}
	}

	public Entity removeEntity(final String name) {
		final Entity entity = entities.remove(name);
		removedEntities.add(entity);
		return entity;
	}
}
