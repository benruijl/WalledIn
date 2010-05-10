package walledin.game.map;

/**
 * This class keeps track of information of a single tile in a map. It stores
 * the tile type and the coordinates.
 * 
 * @author Ben Ruijl
 */
public class Tile {
	private final TileType type;
	private final int x;
	private final int y;

	public Tile(final TileType type, final int x, final int y) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public TileType getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
