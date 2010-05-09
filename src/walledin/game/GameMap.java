package walledin.game;

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
	 * @param name Map name
	 * @param texture Map texture
	 * @param width Width of map
	 * @param height Height of map
	 * @param tiles Tile information
	 * @param z z-index for drawing
	 */
	public GameMap(final String name, final String texture, final int width,
			final int height, final Tile[] tiles) {
		super(name);
		
		setAttribute(Attribute.NAME, name);
		setAttribute(Attribute.WIDTH, width);
		setAttribute(Attribute.HEIGHT, height);

		addBehavior(new MapRenderBehavior(this, texture, width, height, tiles));
	}
}
