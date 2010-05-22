package walledin.network;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.Tile;
import walledin.math.Vector2f;

public class NetworkManager {
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
		for (Map.Entry<Attribute, Object> entry: attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	public void writeCreateEntity(Entity entity, final ByteBuffer buffer) {
		buffer.put(CREATE_ENTITY);
		writeString(entity.getFamilyName(), buffer);
		writeString(entity.getName(), buffer);
		Map<Attribute, Object> attributes = entity.getNetworkAttributes();
		buffer.putInt(attributes.size());
		for (Map.Entry<Attribute, Object> entry: attributes.entrySet()) {
			writeAttribute(entry.getKey(), entry.getValue(), buffer);
		}
	}

	private void writeAttribute(final Attribute attribute, final Object data,
			final ByteBuffer buffer) {
		// Write attribute identification
		buffer.putShort((short) attribute.ordinal());
		switch (attribute) {
		case HEIGHT:
			writeInt((Integer) data, buffer);
			break;
		case ITEM_LIST:
			writeItems((List<Entity>) data, buffer);
			break;
		case POSITION:
			writeVector2f((Vector2f) data, buffer);
			break;
		case TILES:
			writeTiles((List<Tile>) data, buffer);
			break;
		case VELOCITY:
			writeVector2f((Vector2f) data, buffer);
			break;
		case WIDTH:
			writeInt((Integer) data, buffer);
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

	private void writeInt(final int data, final ByteBuffer buffer) {
		buffer.putInt(data);
	}

	private void writeVector2f(final Vector2f data, final ByteBuffer buffer) {
		buffer.putFloat(data.x);
		buffer.putFloat(data.y);
	}
}
