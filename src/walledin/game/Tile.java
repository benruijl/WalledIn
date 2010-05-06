package walledin.game;

import walledin.engine.Rectangle;

/**
 * 
 * @author ben
 */
public class Tile {
	private final int tileNumber;
	private Rectangle mRect;

	public Tile(final int tileNumber) {
		this.tileNumber = tileNumber;
		updateRect();
	}

	public int getTileNumber() {
		return tileNumber;
	}

	public Rectangle getRect() {
		return mRect;
	}

	/**
	 * An update of the texture rectangle is required when the tile number is
	 * set
	 */
	private void updateRect() { // FIXME: hardcoded
		mRect = new Rectangle(tileNumber % 16 * 64.000f / 1024.000f + 1.000f / 1024.000f, tileNumber / 16 * 64.000f / 1024.000f  + 1.000f / 1024.000f,
				64.000f / 1024.000f - 2.000f / 1024.000f,
				64.000f / 1024.000f - 2.000f / 1024.000f); // the offset changes fixes off-by-one errors
	}

}
