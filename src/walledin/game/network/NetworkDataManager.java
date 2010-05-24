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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.Tile;
import walledin.game.map.TileType;

public class NetworkDataManager {
	public static final int DATAGRAM_IDENTIFICATION = 0x47583454;
	public static final byte LOGIN_MESSAGE = 0;
	public static final byte INPUT_MESSAGE = 1;
	public static final byte LOGOUT_MESSAGE = 2;
	public static final byte GAMESTATE_MESSAGE = 3;
	public static final byte CREATE_ENTITY = 1;
	public static final byte REMOVE_ENTITY = 2;
	public static final byte UPDATE_ENTITY = 3;

	public void writeRemoveEntity(Entity entity, final ByteBuffer buffer) {
		buffer.put(REMOVE_ENTITY);
		writeString(entity.getName(), buffer);
	}

	public void writeEntity(Entity entity, final ByteBuffer buffer) {
		buffer.put(UPDATE_ENTITY);
		writeString(entity.getName(), buffer);
		Map<Attribute, Object> attributes = entity.getChangedAttributes();
		buffer.putInt(attributes.size());
		for (Map.Entry<Attribute, Object> entry : attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	public void writeCreateEntity(Entity entity, final ByteBuffer buffer) {
		buffer.put(CREATE_ENTITY);
		writeString(entity.getName(), buffer);
		writeString(entity.getFamilyName(), buffer);
		Map<Attribute, Object> attributes = entity.getNetworkAttributes();
		buffer.putInt(attributes.size());
		for (Map.Entry<Attribute, Object> entry : attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	private void writeAttribute(final Attribute attribute, final Object data,
			final ByteBuffer buffer) {
		// Write attribute identification
		buffer.putShort((short) attribute.ordinal());
		switch (attribute) {
		case HEIGHT:
			buffer.putInt((Integer) data);
			break;
		case WIDTH:
			buffer.putInt((Integer) data);
			break;
		case HEALTH:
			buffer.putInt((Integer) data);
			break;
		case ITEM_LIST:
			writeItems((List<Entity>) data, buffer);
			break;
		case TILES:
			writeTiles((List<Tile>) data, buffer);
			break;
		case POSITION:
			writeVector2f((Vector2f) data, buffer);
			break;
		case VELOCITY:
			writeVector2f((Vector2f) data, buffer);
			break;
		}
	}

	private void writeTiles(final List<Tile> data, final ByteBuffer buffer) {
		buffer.putInt(data.size());
		for (final Tile tile : data) {
			buffer.putInt(tile.getX());
			buffer.putInt(tile.getY());
			buffer.putInt(tile.getType().ordinal());
		}
	}

	private void writeItems(final List<Entity> data, final ByteBuffer buffer) {
		buffer.putInt(data.size());
		for (final Entity item : data) {
			writeString(item.getName(), buffer);
		}
	}

	private void writeString(final String data, final ByteBuffer buffer) {
		buffer.putInt(data.length());
		buffer.put(data.getBytes());
	}

	private void writeVector2f(final Vector2f data, final ByteBuffer buffer) {
		buffer.putFloat(data.x);
		buffer.putFloat(data.y);
	}

	public void readEntity(EntityManager entityManager, ByteBuffer buffer) {
		int type = buffer.get();
		String name = readString(buffer);
		Entity entity = null;
		switch(type) {
		case CREATE_ENTITY:
			String familyName = readString(buffer);
			entity = entityManager.create(familyName, name);
			readAttributes(entity, buffer);
			break;
		case REMOVE_ENTITY:
			entityManager.remove(name);
			break;
		case UPDATE_ENTITY:
			entity = entityManager.get(name);
			readAttributes(entity, buffer);
			break;
		}
	}

	private void readAttributes(Entity entity, ByteBuffer buffer) {
		int num = buffer.getInt();
		for (int i = 0; i < num; i++) {
			readAttribute(entity, buffer);
		}
	}

	private void readAttribute(Entity entity, ByteBuffer buffer) {
		// Write attribute identification
		short ord = buffer.getShort();
		// FIXME dont user ordinal
		Attribute attribute = Attribute.values()[ord];
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
		case ITEM_LIST:
			data = readItems(buffer);
			break;
		case TILES:
			data = readTiles(buffer);
			break;
		case POSITION:
			data = readVector2f(buffer);
			break;
		case VELOCITY:
			data = readVector2f(buffer);
			break;
		}
		entity.setAttribute(attribute, data);
	}

	private Object readVector2f(ByteBuffer buffer) {
		float x = buffer.getFloat();
		float y = buffer.getFloat();
		return new Vector2f(x,y);
	}

	private Object readTiles(ByteBuffer buffer) {
		int size = buffer.getInt();
		List<Tile> tiles = new ArrayList<Tile>();
		for (int i = 0; i < size; i++) {
			int x = buffer.getInt();
			int y = buffer.getInt();
			int ord = buffer.getInt();
			TileType type = TileType.values()[ord];
			Tile tile = new Tile(type, x, y);
			tiles.add(tile);
		}
		return tiles;
	}

	private Object readItems(ByteBuffer buffer) {
		int size = buffer.getInt();
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < size; i++) {
			String name = readString(buffer);
			// how?
			//Entity entity = entityManager.get(name);
			//entities.add(entity);
		}
		return entities;
	}

	private String readString(ByteBuffer buffer) {
		int size = buffer.getInt();
		byte[] bytes = new byte[size];
		buffer.get(bytes);
		return new String(bytes);
	}
}
