package walledin.game;


/**
 * This class keeps track of information of a single tile in a map. It stores
 * whether the tile is solid and the texture rectangle for example.
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
