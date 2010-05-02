package walledin.game;

import walledin.engine.Rectangle;
import walledin.engine.Renderer;

/**
 * This class takes care of maintaining and drawing the tiled map.
 * 
 * @author Ben Ruijl
 */
public class GameMap {
	/* Map data */
	private final String name;
	private final String texture;
	private final int width;
	private final int height;
	private final Tile[] tiles;
	
	public GameMap(final String name, final String texture, final int width,
			final int height, final Tile[] tiles) {
		this.name = name;
		this.texture = texture;
		this.width = width;
		this.height = height;
		this.tiles = tiles;
	}

	public int getHeight() {
		return height;
	}

	public String getName() {
		return name;
	}

	public Tile[] getTiles() {
		return tiles;
	}

	public int getWidth() {
		return width;
	}

	public String getTexture() {
		return texture;
	}

	/**
	 * Creates a new game map. Specify a reader to choose the file format to
	 * load form. Use MapReaderXML for example to load from an XML file.
	 * 
	 * @param reader
	 *            specifies the class that will load the actual data Each
	 *            MapReader can load from a different format.
	 */
	/*
	 * public GameMap() { this.reader = reader; }
	 */
	/**
	 * Loads a map from a file. It uses the reader defined in the map
	 * constructor to load the actual data.
	 * 
	 * @param filename
	 *            The filename or resource name to load the map from
	 * @return Returns true on success and false on failure
	 */

	/*
	 * public boolean ReadFromFile(String filename) { return
	 * reader.readFromFile(this, filename); }
	 */
	/**
	 * Draws the map
	 * 
	 * @param renderer
	 *            The renderer to be used
	 */
	public void draw(final Renderer renderer) {
		final float tileWidth = 32.0f;
		final int stepSize = 10;

		/* Partition the map */
		for (int sw = 0; sw < width; sw += stepSize) // width % stepSize should
														// be 0
		{
			for (int sh = 0; sh < height; sh += stepSize) {
				if (renderer
						.inFrustum(new Rectangle(sw * tileWidth,
								sh * tileWidth, tileWidth * stepSize, tileWidth
										* stepSize))) {
					for (int i = 0; i < java.lang.Math.min(stepSize, height
							- sh); i++) {
						for (int j = 0; j < java.lang.Math.min(stepSize, width
								- sw); j++) {
							final Tile tile = tiles[(sh + i) * width + sw + j];
							renderer.drawRect(texture, tile.getRect(),
									new Rectangle((sw + j) * tileWidth,
											(sh + i) * tileWidth, tileWidth,
											tileWidth));
						}
					}
				}

			}

			/*
			 * for (int i = 0; i < height; i++) { for (int j = 0; j < width;
			 * j++) { Tile tile = tiles[i * width + j];
			 * renderer.drawRect("tiles", new Rectangle((tile.getTileNumber() %
			 * 16) * 64.0f, (tile.getTileNumber() / 16) * 64.0f, 64.0f, 64.0f),
			 * new Rectangle(j * tileWidth, i * tileWidth, tileWidth,
			 * tileWidth)); } }
			 */
		}

	}
}
