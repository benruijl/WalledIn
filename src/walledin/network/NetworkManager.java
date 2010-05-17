package walledin.network;

import java.nio.ByteBuffer;
import java.util.List;

import walledin.engine.math.Vector2f;
import walledin.game.Item;
import walledin.game.entity.Attribute;
import walledin.game.map.Tile;

public class NetworkManager {
	public void writeAttribute(final Attribute attribute, final Object data,
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
			buffer.put(item.getName().getBytes());
		}
	}

	private void writeInt(final int data, final ByteBuffer buffer) {
		buffer.putInt(data);
	}

	private void writeVector2f(final Vector2f data, final ByteBuffer buffer) {
		buffer.putFloat(data.x);
		buffer.putFloat(data.y);
	}
}
