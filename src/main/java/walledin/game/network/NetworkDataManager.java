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
package walledin.game.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.Tile;
import walledin.game.map.TileType;

public class NetworkDataManager {
	public static final byte ALIVE_MESSAGE = 3;
	private static final int BUFFER_SIZE = 1024 * 1024;
	public static final byte CREATE_ENTITY = 1;
	public static final int DATAGRAM_IDENTIFICATION = 0x47583454;
	public static final byte GAMESTATE_MESSAGE = 4;
	public static final byte INPUT_MESSAGE = 1;
	private final static Logger LOG = Logger
			.getLogger(NetworkDataManager.class);
	public static final byte LOGIN_MESSAGE = 0;
	public static final byte LOGOUT_MESSAGE = 2;
	public static final byte REMOVE_ENTITY = 2;
	public static final byte UPDATE_ENTITY = 3;
	private final ByteBuffer buffer;
	private final NetworkEventListener listener;

	public NetworkDataManager(final NetworkEventListener listener) {
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.listener = listener;
	}

	public String getAddressRepresentation(final SocketAddress address) {
		final InetSocketAddress inetAddr = (InetSocketAddress) address;
		return inetAddr.getAddress().getHostAddress() + "@"
				+ inetAddr.getPort();
	}

	private void prepareAliveMessage() {
		buffer.clear();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.ALIVE_MESSAGE);
		buffer.flip();
	}

	/**
	 * This function calculates the changes of the current gamestate to the
	 * previous gamestate and puts this delta gamestate in a buffer.
	 */
	public void prepareGamestateMessageExistingPlayers(
			final EntityManager entityManager) {
		buffer.clear();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.GAMESTATE_MESSAGE);
		final Set<Entity> removedEntities = entityManager.getRemoved();
		final Set<Entity> createdEntities = entityManager.getCreated();
		final Collection<Entity> entities = entityManager.getAll();
		buffer.putInt(entities.size() + removedEntities.size());
		for (final Entity entity : entities) {
			if (createdEntities.contains(entity)) {
				writeCreateEntityData(entity, buffer);
			} else {
				writeEntityData(entity, buffer);
			}
		}
		for (final Entity entity : removedEntities) {
			writeRemoveEntityData(entity, buffer);
		}
		buffer.flip();
	}

	/**
	 * This function writes the entire current gamestate to a buffer. Used for
	 * new players only. Current players use the
	 * <code>prepareGamestateMessageExistingPlayers</code> function.
	 */
	public void prepareGamestateMessageNewPlayers(
			final EntityManager entityManager) {
		buffer.clear();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.GAMESTATE_MESSAGE);
		final Collection<Entity> entities = entityManager.getAll();
		buffer.putInt(entities.size());
		for (final Entity entity : entities) {
			writeCreateEntityData(entity, buffer);
		}
		buffer.flip();
	}

	private void processAliveMessage(final SocketAddress address)
			throws IOException {
		listener.receivedAliveMessage(address);
	}

	private void processGamestateMessage(final EntityManager entityManager,
			final SocketAddress address) throws IOException {
		final int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			readEntityData(entityManager, buffer);
		}
		listener.receivedGamestateMessage(address);
	}

	private void processInputMessage(final SocketAddress address) {
		final short numKeys = buffer.getShort();
		final Set<Integer> keys = new HashSet<Integer>();
		for (int i = 0; i < numKeys; i++) {
			keys.add((int) buffer.getShort());
		}
		listener.receivedInputMessage(address, keys);
	}

	private void processLoginMessage(final SocketAddress address) {
		final int nameLength = buffer.getInt();
		final byte[] nameBytes = new byte[nameLength];
		buffer.get(nameBytes);
		final String name = new String(nameBytes);
		listener.receivedLoginMessage(address, name);
	}

	private void processLogoutMessage(final SocketAddress address) {
		listener.receivedLogoutMessage(address);
	}

	private void readAttributeData(final Entity entity, final ByteBuffer buffer) {
		// Write attribute identification
		final short ord = buffer.getShort();
		// FIXME dont user ordinal
		final Attribute attribute = Attribute.values()[ord];
		Object data = null;
		switch (attribute) {
		case HEIGHT:
			data = buffer.getInt();
			break;
		case WIDTH:
			data = buffer.getInt();
			break;
		case HEALTH:
			data = buffer.getInt();
			break;
		case ORIENTATION:
			data = buffer.getInt();
			break;
		case PLAYER_NAME:
			data = readStringData(buffer);
			break;
		case ITEM_LIST:
			data = readItemsData(buffer);
			break;
		case TILES:
			data = readTilesData(buffer);
			break;
		case POSITION:
			data = readVector2fData(buffer);
			break;
		case VELOCITY:
			data = readVector2fData(buffer);
			break;
		}
		entity.setAttribute(attribute, data);
	}

	private void readAttributesData(final Entity entity, final ByteBuffer buffer) {
		final int num = buffer.getInt();
		for (int i = 0; i < num; i++) {
			readAttributeData(entity, buffer);
		}
	}

	public void readEntityData(final EntityManager entityManager,
			final ByteBuffer buffer) {
		final int type = buffer.get();
		final String name = readStringData(buffer);
		Entity entity = null;
		switch (type) {
		case CREATE_ENTITY:
			final String familyName = readStringData(buffer);
			entity = entityManager.create(familyName, name);
			readAttributesData(entity, buffer);
			break;
		case REMOVE_ENTITY:
			entityManager.remove(name);
			break;
		case UPDATE_ENTITY:
			entity = entityManager.get(name);
			readAttributesData(entity, buffer);
			break;
		}
	}

	private Object readItemsData(final ByteBuffer buffer) {
		final int size = buffer.getInt();
		final List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < size; i++) {
			final String name = readStringData(buffer);
			// how?
			// Entity entity = entityManager.get(name);
			// entities.add(entity);
		}
		return entities;
	}

	private String readStringData(final ByteBuffer buffer) {
		final int size = buffer.getInt();
		final byte[] bytes = new byte[size];
		buffer.get(bytes);
		return new String(bytes);
	}

	private Object readTilesData(final ByteBuffer buffer) {
		final int size = buffer.getInt();
		final List<Tile> tiles = new ArrayList<Tile>();
		for (int i = 0; i < size; i++) {
			final int x = buffer.getInt();
			final int y = buffer.getInt();
			final int ord = buffer.getInt();
			final TileType type = TileType.values()[ord];
			final Tile tile = new Tile(type, x, y);
			tiles.add(tile);
		}
		return tiles;
	}

	private Object readVector2fData(final ByteBuffer buffer) {
		final float x = buffer.getFloat();
		final float y = buffer.getFloat();
		return new Vector2f(x, y);
	}

	/**
	 * Reads a datagram from the channel if there is one
	 * 
	 * @param channel
	 *            The channel to read from
	 * @param entityManager
	 *            the entity manager to process the changes in
	 * @return true if a datagram was present on the channel, else false
	 * @throws IOException
	 */
	public boolean recieveMessage(final DatagramChannel channel,
			final EntityManager entityManager) throws IOException {
		int ident = -1;
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		final SocketAddress address = channel.receive(buffer);
		if (address == null) {
			return false;
		}
		buffer.flip();
		ident = buffer.getInt();
		if (ident != NetworkDataManager.DATAGRAM_IDENTIFICATION) {
			// ignore the datagram, incorrect format
			return true;
		}
		final byte type = buffer.get();
		switch (type) {
		case NetworkDataManager.ALIVE_MESSAGE:
			processAliveMessage(address);
			break;
		case NetworkDataManager.GAMESTATE_MESSAGE:
			processGamestateMessage(entityManager, address);
			break;
		case NetworkDataManager.LOGIN_MESSAGE:
			processLoginMessage(address);
			break;
		case NetworkDataManager.LOGOUT_MESSAGE:
			processLogoutMessage(address);
			break;
		case NetworkDataManager.INPUT_MESSAGE:
			processInputMessage(address);
			break;
		default:
			LOG.warn("Received unhandled message");
			break;
		}
		return true;
	}

	public void sendAliveMessage(final DatagramChannel channel)
			throws IOException {
		prepareAliveMessage();
		channel.write(buffer);
	}

	public void sendAliveMessage(final DatagramChannel channel,
			final SocketAddress address) throws IOException {
		prepareAliveMessage();
		sendCurrentMessage(channel, address);
	}

	public void sendCurrentMessage(final DatagramChannel channel,
			final SocketAddress address) throws IOException {
		buffer.rewind();
		channel.send(buffer, address);
	}

	public void sendInputMessage(final DatagramChannel channel,
			final Set<Integer> keysDown) throws IOException {
		buffer.limit(BUFFER_SIZE);
		buffer.rewind();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.INPUT_MESSAGE);
		buffer.putShort((short) keysDown.size());
		for (final int key : keysDown) {
			buffer.putShort((short) key);
		}
		buffer.flip();
		channel.write(buffer);
	}

	public void sendLoginMessage(final DatagramChannel channel,
			final String username) throws IOException {
		buffer.clear();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.LOGIN_MESSAGE);
		buffer.putInt(username.length());
		buffer.put(username.getBytes());
		buffer.flip();
		channel.write(buffer);
	}

	public void sendLogoutMessage(final DatagramChannel channel)
			throws IOException {
		buffer.clear();
		buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
		buffer.put(NetworkDataManager.LOGOUT_MESSAGE);
		buffer.flip();
		channel.write(buffer);
	}

	private void writeAttribute(final Attribute attribute, final Object data,
			final ByteBuffer buffer) {
		// Write attribute identification
		buffer.putShort((short) attribute.ordinal());
		switch (attribute) {
		case HEIGHT:
			writeIntegerData((Integer) data, buffer);
			break;
		case WIDTH:
			writeIntegerData((Integer) data, buffer);
			break;
		case HEALTH:
			writeIntegerData((Integer) data, buffer);
			break;
		case ORIENTATION:
			writeIntegerData((Integer) data, buffer);
			break;
		case PLAYER_NAME:
			writeStringData((String) data, buffer);
			break;
		case ITEM_LIST:
			writeItemsData((List<Entity>) data, buffer);
			break;
		case TILES:
			writeTilesData((List<Tile>) data, buffer);
			break;
		case POSITION:
			writeVector2fData((Vector2f) data, buffer);
			break;
		case VELOCITY:
			writeVector2fData((Vector2f) data, buffer);
			break;
		}
	}

	public void writeCreateEntityData(final Entity entity,
			final ByteBuffer buffer) {
		buffer.put(CREATE_ENTITY);
		writeStringData(entity.getName(), buffer);
		writeStringData(entity.getFamilyName(), buffer);
		final Map<Attribute, Object> attributes = entity.getNetworkAttributes();
		buffer.putInt(attributes.size());
		for (final Map.Entry<Attribute, Object> entry : attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	public void writeEntityData(final Entity entity, final ByteBuffer buffer) {
		buffer.put(UPDATE_ENTITY);
		writeStringData(entity.getName(), buffer);
		final Map<Attribute, Object> attributes = entity.getChangedAttributes();
		buffer.putInt(attributes.size());
		for (final Map.Entry<Attribute, Object> entry : attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	private void writeIntegerData(final int data, final ByteBuffer buffer) {
		buffer.putInt(data);
	}

	private void writeItemsData(final List<Entity> data, final ByteBuffer buffer) {
		buffer.putInt(data.size());
		for (final Entity item : data) {
			writeStringData(item.getName(), buffer);
		}
	}

	public void writeRemoveEntityData(final Entity entity,
			final ByteBuffer buffer) {
		buffer.put(REMOVE_ENTITY);
		writeStringData(entity.getName(), buffer);
	}

	private void writeStringData(final String data, final ByteBuffer buffer) {
		buffer.putInt(data.length());
		buffer.put(data.getBytes());
	}

	private void writeTilesData(final List<Tile> data, final ByteBuffer buffer) {
		buffer.putInt(data.size());
		for (final Tile tile : data) {
			buffer.putInt(tile.getX());
			buffer.putInt(tile.getY());
			buffer.putInt(tile.getType().ordinal());
		}
	}

	private void writeVector2fData(final Vector2f data, final ByteBuffer buffer) {
		buffer.putFloat(data.x);
		buffer.putFloat(data.y);
	}
}
