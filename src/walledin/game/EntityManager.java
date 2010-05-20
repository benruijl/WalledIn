package walledin.game;

import java.util.HashMap;
import java.util.Map;

import walledin.game.entity.Entity;

public class EntityManager {
	private static final EntityManager INSTANCE = new EntityManager();

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static EntityManager getInstance() {
		return INSTANCE;
	}

	private EntityManager() {
		entities = new HashMap<String, Entity>();
		factory = new EntityFactory();
	}
	
	
	private Map<String, Entity> entities;
	private DrawOrderManager drawOrderManager;
	private EntityFactory factory;
	
	/**
	 * Creates a new Entity and adds it to the entities list.
	 * @param familyName
	 * @param name
	 * @return The created entity or null on failure
	 */
	public Entity create(String familyName, String entityName)
	{
		return factory.create(familyName, entityName);
	}
	
	public void add(Entity entity)
	{
		entities.put(entity.getName(), entity);
	}
	
	public void remove(String name)
	{
		entities.remove(name);
	}
	
	public Entity get(String name)
	{
		return entities.get(name);
	}
}
