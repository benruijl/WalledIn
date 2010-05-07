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
	public GameMap(final String name, final String texture, final int width,
			final int height, final Tile[] tiles) {
		super(name);
		
		setAttribute(Attribute.NAME, name);
		setAttribute(Attribute.WIDTH, width);
		setAttribute(Attribute.HEIGHT, height);

		addBehavior(new MapRenderBehavior(this, texture, width, height, tiles));
	}
}
