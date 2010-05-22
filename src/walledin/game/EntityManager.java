package walledin.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class EntityManager {
	private Map<String, Entity> entities;
	private DrawOrderManager drawOrderManager;
	private EntityFactory factory;
	private int uniqueNameCount = 0;
	private Set<Entity> removed;
	private Set<Entity> created;
	
	public EntityManager(EntityFactory factory) {
		entities = new HashMap<String, Entity>();
		removed = new HashSet<Entity>();
		created = new HashSet<Entity>();
		this.factory = factory;
		drawOrderManager = new DrawOrderManager();
	}
	
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
		created.add(entity);
		return entity;
	}
	
	/**
	 * Generates a unique name for an object. Useful when generating entities in
	 * runtime. The entities will be named in the following format: ENT_<i>familyname</i>_<i>num</i>,
	 * where <i>familyname</i> is the family name and <i>num</i> is the number of already generated
	 * unique names.
	 * @param familyName Name of the item's family
	 * @return Unique entity name
	 */
	public String generateUniqueName(String familyName)
	{
		uniqueNameCount++;
		return "ENT_" + familyName + "_" + Integer.toString(uniqueNameCount);
	}
	
	/**
	 * Adds an entity to the list.
	 * @param entity Entity to add
	 */
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
		removed.add(entity);
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
		// Clear the removed and created entities.
		removed = new HashSet<Entity>();
		created = new HashSet<Entity>();
		
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

	public void init() {
		factory.loadItemsFromXML("data/items.xml");
	}

	public Set<Entity> getRemoved() {
		return removed;
	}
	
	public Set<Entity> getCreated() {
		return created;
	}

	public Collection<Entity> getAll() {
		// TODO maybe only do readonly view...
		return entities.values();
	}
}
