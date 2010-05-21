package walledin.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.math.Rectangle;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.BackgroundRenderBehavior;
import walledin.game.entity.behaviors.HealthBehavior;
import walledin.game.entity.behaviors.MapRenderBehavior;
import walledin.game.entity.behaviors.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.PlayerRenderBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;
import walledin.game.map.Tile;

public class EntityFactory {
	Map<String, EntityConstructionFunction> entityContructionFunctions;
	
	private interface EntityConstructionFunction {
		Entity create(final Entity ent);
	}
	
	public EntityFactory()
	{
		entityContructionFunctions = new HashMap<String, EntityConstructionFunction>();
		addCreationFunctions();
	}
	
	public Entity create(final String familyName, final String entityName) {
		final Entity ent = new Entity(familyName, entityName);
		EntityConstructionFunction func = entityContructionFunctions.get(familyName);
		
		if (func == null)
			return new Entity(familyName, entityName); // return generic entity
		
		return func.create(ent);
	}
	
	private Entity createPlayer(final Entity player) {
		player.setAttribute(Attribute.ORIENTATION, 1); // start looking to
		// the right

		player.addBehavior(new HealthBehavior(player, 100, 100));
		player.addBehavior(new PlayerControlBehaviour(player, null, null));
		player.addBehavior(new PlayerAnimationBehavior(player));
		player.addBehavior(new PlayerRenderBehavior(player));

		// FIXME correct the drawing instead of the hack the bounding box
		player.setAttribute(Attribute.BOUNDING_RECT,
				new Rectangle(0, 0, 44, 43));

		return player;
	}

	private Entity createBackground(final Entity ent) {
		ent.addBehavior(new BackgroundRenderBehavior(ent));
		return ent;
	}

	
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
	public Entity createGameMap(final String name, final int width,
			final int height, final List<Tile> tiles, final List<Item> items) {
		final Entity map = new Entity("Map", name);

		map.setAttribute(Attribute.WIDTH, width);
		map.setAttribute(Attribute.HEIGHT, height);
		map.setAttribute(Attribute.TILES, tiles);
		map.setAttribute(Attribute.ITEM_LIST, items);
		
		map.addBehavior(new SpatialBehavior(map, null, null));
		map.addBehavior(new MapRenderBehavior(map, width, height, tiles));

		return map;
	}
	
	private void addCreationFunctions() {		
			entityContructionFunctions.put("Player",
					new EntityConstructionFunction() {

						@Override
						public Entity create(Entity ent) {
							return createPlayer(ent);
						}
					});
		
			entityContructionFunctions.put("Background",
					new EntityConstructionFunction() {

						@Override
						public Entity create(Entity ent) {
							return createBackground(ent);
						}
					});
	}


}
