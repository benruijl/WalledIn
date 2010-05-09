package walledin.game;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import walledin.engine.TextureManager;
import walledin.util.XMLReader;

/**
 * Loads a map from an XML file
 * 
 * @author ben
 */
public class GameMapIOXML implements GameMapIO {
	/**
	 * Reads tile information
	 * @param reader XML reader
	 * @param el Current element
	 * @return An array of tiles
	 */
	private Tile[] parseTiles(final XMLReader reader, final Element el) {
		final String tiles = reader.getTextValue(el, "Tiles");

		final StringTokenizer st = new StringTokenizer(tiles);
		final ArrayList<Tile> til = new ArrayList<Tile>();

		while (st.hasMoreTokens()) {
			final String s = st.nextToken();

			// FIXME: expand the XML file so that it reads information about the
			// tile texture
			til.add(new Tile(Integer.parseInt(s), 16, 64, 64, 1024, 1024, false));
		}

		return til.toArray(new Tile[0]);
	}

	/**
	 * Reads map data from an XML file
	 * 
	 * @param filename
	 *            Filename of XML data
	 * @return Returns true on success and false on failure
	 */
	@Override
	public GameMap readFromFile(final String filename) {
		XMLReader reader = new XMLReader();

		if (reader.open(filename)) {
			List<Element> elList = reader.getElements(reader.getRootElement(),
					"Map");

			// assumes there is only one map
			if (elList != null && elList.size() > 0) {
				Element map = elList.get(0);

				final String name = reader.getTextValue(map, "Name");
				final String tex = reader.getTextValue(map, "Texture");
				final int width = reader.getIntValue(map, "Width");
				final int height = reader.getIntValue(map, "Height");
				final Tile[] tiles = parseTiles(reader, map);

				final String texRes = TextureManager.getInstance()
						.loadFromFile(tex);

				final GameMap m = new GameMap(name, texRes, width, height,
						tiles);

				return m;
			} else
				return null;
		}

		return null;
	}

	@Override
	public boolean writeToFile(final GameMap map, final String filename) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
