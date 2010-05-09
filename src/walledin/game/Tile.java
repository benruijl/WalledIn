package walledin.game;

import walledin.engine.math.Rectangle;

/**
 * This class keeps track of information of a single tile in a map. It stores
 * whether the tile is solid and the texture rectangle for example.
 * 
 * @author Ben Ruijl
 */
public class Tile {
	private final int tileNumber;
	private Rectangle texRect;
	private boolean solid;

	public Tile(final int tileNumber, final int tileNumPerLine,
			final int tileWidth, final int tileHeight, final int texWidth,
			final int texHeight, boolean solid) {

		this.tileNumber = tileNumber;
		this.solid = solid;

		// the offset fixes off-by-one errors
		this.texRect = new Rectangle((tileNumber % 16 * tileWidth + 1.000f)
				/ texWidth,
				(tileNumber / 16 * tileHeight + 1.000f) / texHeight,
				(tileWidth - 2.000f) / texWidth, (tileHeight - 2.000f)
						/ texHeight);
	}

	public int getTileNumber() {
		return tileNumber;
	}

	public boolean isSolid() {
		return solid;
	}

	public Rectangle getTexRect() {
		return texRect;
	}
}
