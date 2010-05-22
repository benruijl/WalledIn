package walledin.game;

import java.util.HashMap;
import java.util.Map;

import walledin.game.entity.Entity;

public abstract class AbstractEntityFactory implements EntityFactory {
	protected interface EntityConstructionFunction {
		Entity create(final Entity ent);
	}

	protected final Map<String, EntityConstructionFunction> entityContructionFunctions;

	public AbstractEntityFactory() {
		entityContructionFunctions = new HashMap<String, EntityConstructionFunction>();
	}

	/**
	 * @see walledin.game.EntityFactory#create(java.lang.String,
	 *      java.lang.String)
	 */
	public Entity create(final String familyName, final String entityName) {
		final Entity ent = new Entity(familyName, entityName);
		final EntityConstructionFunction func = entityContructionFunctions
				.get(familyName);

		if (func == null) {
			return ent; // return generic entity
		}

		return func.create(ent);
	}

	@Override
	public abstract boolean loadItemsFromXML(String filename);

}