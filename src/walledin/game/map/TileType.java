package walledin.game.map;

import java.util.HashMap;
import java.util.Map;

public enum TileType {
	TILE_EMPTY(' ', false),
	TILE_FILLED('#', true),
	TILE_TOP_GRASS_END_LEFT('C', true),
	TILE_TOP_GRASS_END_RIGHT('D', true),
	TILE_TOP_GRASS('E', true),
	TILE_LEFT_GRASS('F', true),
	TILE_LEFT_MUD('G', true),
	TILE_RIGHT_MUD('H', true),
	TILE_TOP_LEFT_GRASS('I', true),
	TILE_BOTTOM_LEFT_MUD('J', true),
	TILE_BOTTOM_RIGHT_MUD('K', true),
	TILE_TOP_LEFT_GRASS_END('L', true),
	TILE_BOTTOM_MUD('M', true);

	private static final Map<Character, TileType> MAPPING = new HashMap<Character, TileType>();
	private final char mapChar;
	private final boolean solid;

	static {
		for (final TileType tile : values()) {
			MAPPING.put(tile.mapChar, tile);
		}
	}

	private TileType(final char mapChar, final boolean solid) {
		this.mapChar = mapChar;
		this.solid = solid;
	}

	public static TileType getTile(final char mapChar) {
		return MAPPING.get(mapChar);
	}

	public String getTexturePartID() {
		return name().toLowerCase();
	}

	public boolean isSolid() {
		return solid;
	}
}
