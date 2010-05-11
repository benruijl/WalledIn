package walledin.game.map;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import walledin.engine.math.Vector2f;
import walledin.game.Item;
import walledin.game.ItemFactory;
import walledin.game.entity.Attribute;
import walledin.util.XMLReader;

/**
 * Loads a map from an XML file
 * 
 * @author ben
 */
public class GameMapIOXML implements GameMapIO {
	// FIXME dont use instance vars for this...
	private int width;
	private int height;

	/**
	 * Reads tile information
	 * 
	 * @param reader
	 *            XML reader
	 * @param element
	 *            Current element
	 * @return An list of tiles
	 */
	private List<Tile> parseTiles(final Element element) {
		final List<Tile> result = new ArrayList<Tile>();
		final String tiles = XMLReader.getTextValue(element, "tiles");
		final String[] rows = tiles.split("\n");
		int x = 0;
		int y = 0;
		for (final String row : rows) {
			if (row.trim().isEmpty()) {
				continue;
			}
			y = 0;
			for (final char mapChar : row.trim().toCharArray()) {
				final TileType type = TileType.getTile(mapChar);
				final Tile tile = new Tile(type, x, y);
				result.add(tile);
				y++;
			}
			x++;
		}
		height = x;
		width = y;
		return result;
	}
	
	private List<Item> parseItems(final Element element)
	{
		List<Item> itList = new ArrayList<Item>();
		Element itemsNode = XMLReader.getFirstElement(element, "items");
		List<Element> items = XMLReader.getElements(itemsNode, "item");
		
		for (Element el : items)
		{
			String name = el.getAttribute("name");
			String type = el.getAttribute("type");
			int x = Integer.parseInt(el.getAttribute("x"));
			int y = Integer.parseInt(el.getAttribute("y"));
			
			Item item = ItemFactory.getInstance().create(type, name);
			item.setAttribute(Attribute.POSITION, new Vector2f(x, y));
			itList.add(item);
		}
		
		return itList;
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
		final XMLReader reader = new XMLReader();

		if (reader.open(filename)) {
			final Element map = reader.getRootElement();

			final String name = XMLReader.getTextValue(map, "name");
			final List<Item> items = parseItems(map);
			final List<Tile> tiles = parseTiles(map);

			final GameMap m = new GameMap(name, width, height, tiles, items);

			return m;
		} else {
			return null;
		}

	}

	@Override
	public boolean writeToFile(final GameMap map, final String filename) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
