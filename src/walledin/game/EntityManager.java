package walledin.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
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
		drawOrderManager = new DrawOrderManager();
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
		Entity entity = factory.create(familyName, entityName);
		add(entity);
		return entity;
	}
	
	public void add(Entity entity)
	{
		entities.put(entity.getName(), entity);
		
		if (entity.hasAttribute(Attribute.Z_INDEX))
			drawOrderManager.add(entity);
	}
	
	public void add(final Collection<Entity> entitiesList) {
		for (final Entity en : entitiesList)
			add(en);
	}
	
	public Entity remove(String name)
	{
		Entity entity = entities.remove(name);
		drawOrderManager.removeEntity(entity);
		return entity;
	}
	
	public Entity get(String name)
	{
		return entities.get(name);
	}
	
	public void draw(Renderer renderer)
	{
		drawOrderManager.draw(renderer);
	}
	
	public void update(double delta)
	{
		/* Clean up entities which are flagged for removal */
		List<Entity> removeList = new ArrayList<Entity>();
		for (final Entity entity : entities.values())
			if (entity.isMarkedRemoved())
				removeList.add(entity);
		
		for (int i = 0; i < removeList.size(); i++)
			remove(removeList.get(i).getName());
		
		
		for (final Entity entity : entities.values()) {
			entity.sendUpdate(delta);
		}
	}
	
	public void doCollisionDetection(Entity curMap, double delta)
	{
		CollisionManager.calculateMapCollisions(curMap,
				entities.values(), delta);
		CollisionManager.calculateEntityCollisions(entities.values(), delta);
	}
}
