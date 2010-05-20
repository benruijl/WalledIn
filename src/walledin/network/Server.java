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
import walledin.game.Item;
import walledin.game.ItemFactory;
import walledin.game.Player;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMap;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.math.Vector2f;

public class Server {
	private static final int DATAGRAM_IDENTIFICATION = 0x47583454;
	private static final int PORT = 1234;
	private static final int BUFFER_SIZE = 1024 * 1024;
	private static final byte LOGIN_MESSAGE = 0;
	private static final byte INPUT_MESSAGE = 1;
	private static final byte LOGOUT_MESSAGE = 2;
	private Map<SocketAddress, Player> players;
	private Map<Player, Set<Integer>> keysDown;
	private Map<String, Entity> entities;
	private boolean running;
	private ByteBuffer buffer;

	public Server() {
		entities = new LinkedHashMap<String, Entity>();
		players = new HashMap<SocketAddress, Player>();
		running = false;
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
	}

	public void run() throws IOException {
		init();
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress(PORT));
		channel.configureBlocking(false);
		
		double delta = 0.001;
		long time = System.nanoTime();
		while (running) {
			readDatagrams(channel);
			delta = System.nanoTime() - time;
			update(delta);
			writeDatagrams(channel);
		}
	}

	private void writeDatagrams(DatagramChannel channel) {
			
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
		if (ident == DATAGRAM_IDENTIFICATION) {
			byte type = buffer.get();
			switch (type) {
			case LOGIN_MESSAGE:
				int nameLength = buffer.getInt();
				byte[] nameBytes = new byte[nameLength];
				buffer.get(nameBytes);
				String name = new String(nameBytes);
				createPlayer(name, address);
				break;
			case LOGOUT_MESSAGE:
				removePlayer(address);
			case INPUT_MESSAGE:
				short numKeys = buffer.getShort();
				Set<Integer> keys = new HashSet<Integer>();
				for (int i=0; i < numKeys; i++) {
					keys.add((int) buffer.getShort());
				}
				Player player = players.get(address);
				keysDown.put(player, keys);
				break;
			}
		}
	}

	private void removePlayer(SocketAddress address) {
		Player player = players.get(address);
		newPlayers.remove(player);
		removeEntity(player.getName());
	}

	private void createPlayer(String name, SocketAddress address) {
		Player player = new Player(name);
		newEntity(player);
		newPlayers.add(player);
		player.setAttribute(Attribute.POSITION,
				new Vector2f(400, 300));
		players.put(address, player);
	}

	private void newEntity(Entity entity) {
		entities.put(entity.getName(), entity);
	}

	public void update(final double delta) {
		/* Update all entities */
		for (final Entity entity : entities.values()) {
			entity.sendUpdate(delta);
		}

		/* Do collision detection */
		CollisionManager.calculateMapCollisions((GameMap) entities.get("Map"),
				entities.values(), delta);
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

		final GameMapIO mMapIO = new GameMapIOXML(); // choose XML as format

		newEntity(mMapIO.readFromFile("data/map.xml"));

		// add map items like healthkits to entity list
		final List<Item> mapItems = entities.get("Map").getAttribute(
				Attribute.ITEM_LIST);
		for (final Item item : mapItems) {
			newEntity(item);
		}
	}

	public Entity removeEntity(final String name) {
		final Entity entity = entities.remove(name);
		return entity;
	}
}
