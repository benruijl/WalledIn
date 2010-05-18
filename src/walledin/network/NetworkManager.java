package walledin.network;

import java.nio.ByteBuffer;
import java.util.List;

import walledin.game.Item;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.Tile;
import walledin.math.Vector2f;

public class NetworkManager {
	private void writeEntity(final Entity entity, final ByteBuffer buffer) {
		writeString(entity.getFamilyName(), buffer);
		writeString(entity.getName(), buffer);
		// TODO write the attribs
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
			writeItems((List<Item>) data, buffer);
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

	private void writeItems(final List<Item> data, final ByteBuffer buffer) {
		buffer.putInt(data.size());
		for (final Item item : data) {
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
