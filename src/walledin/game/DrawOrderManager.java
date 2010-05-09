package walledin.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class DrawOrderManager implements Comparator<Entity> {
	SortedSet<Entity> entityList;
	
	public DrawOrderManager() {
		super();
		this.entityList = new TreeSet<Entity>(this);
	}
	
	/**
	 * Add a list of entities to a list sorted on z-index
	 * @param Collection of entities to be added
	 */
	public void add(Collection<Entity> Collection)
	{
		for (Entity en : Collection)
		{
			if (en.hasAttribute(Attribute.Z_INDEX))
				entityList.add(en);
		}
	}
	
	/**
	 * Add entity to a list sorted on z-index
	 * @param e Entity to be added
	 * @return True if added, false if not
	 */
	public boolean add(Entity e)
	{
		if (!e.hasAttribute(Attribute.Z_INDEX))
			return false;
		
		return entityList.add(e);
	}
	
	public SortedSet<Entity> getList()
	{
		return entityList;
	}
	
	public void draw(Renderer renderer)
	{
		/* Draw all entities in the correct order */
		for (Entity ent : entityList)
			ent.sendMessage(MessageType.RENDER, renderer);
	}

	@Override
	public int compare(Entity o1, Entity o2) {
		int zA = (Integer)o1.getAttribute(Attribute.Z_INDEX);
		int zB = (Integer)o2.getAttribute(Attribute.Z_INDEX);
		
		if (zA == zB)
			return o1.getName().compareTo(o2.getName());
		
		return zA - zB;
	}
	
	
}
