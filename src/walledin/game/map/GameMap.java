package walledin.game.map;

import java.util.List;

import walledin.game.Item;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.MapRenderBehavior;

/**
 * This class takes care of maintaining and drawing the tiled map.
 * 
 * @author Ben Ruijl
 */
public class GameMap extends Entity {

	/**
	 * Creates a new game map
	 * 
	 * @param name
	 *            Map name
	 * @param width
	 *            Width of map
	 * @param height
	 *            Height of map
	 * @param tiles
	 *            Tile information
	 * @param items
	 */
	public GameMap(final String name, final int width, final int height,
			final List<Tile> tiles, final List<Item> items) {
		super(name, "map");

		setAttribute(Attribute.WIDTH, width);
		setAttribute(Attribute.HEIGHT, height);
		setAttribute(Attribute.TILES, tiles);
		setAttribute(Attribute.ITEM_LIST, items);

		addBehavior(new MapRenderBehavior(this, width, height, tiles));
	}
}
