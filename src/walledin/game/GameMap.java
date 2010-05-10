package walledin.game;

import java.util.List;

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
	 */
	public GameMap(final String name, final int width, final int height,
			final List<Tile> tiles) {
		super(name);

		setAttribute(Attribute.NAME, name);
		setAttribute(Attribute.WIDTH, width);
		setAttribute(Attribute.HEIGHT, height);

		addBehavior(new MapRenderBehavior(this, width, height, tiles));
	}
}
