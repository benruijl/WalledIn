package walledin.game;

import java.util.HashMap;
import java.util.Map;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.BackgroundRenderBehavior;
import walledin.game.entity.behaviors.HealthBehavior;
import walledin.game.entity.behaviors.MapRenderBehavior;
import walledin.game.entity.behaviors.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.PlayerRenderBehavior;
import walledin.math.Rectangle;

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
			return ent; // return generic entity
		
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
	
	private Entity createGameMap(final Entity map) {
		map.addBehavior(new MapRenderBehavior(map));
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
			
			entityContructionFunctions.put("Map",
					new EntityConstructionFunction() {

						@Override
						public Entity create(Entity ent) {
							return createGameMap(ent);
						}
					});
	}


}
