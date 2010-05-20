package walledin.game;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class DrawOrderManager {
	private static class ZOrderComperator implements Comparator<Entity> {
		@Override
		public int compare(final Entity o1, final Entity o2) {
			final int zA = (Integer) o1.getAttribute(Attribute.Z_INDEX);
			final int zB = (Integer) o2.getAttribute(Attribute.Z_INDEX);

			if (zA == zB) {
				return o1.getName().compareTo(o2.getName());
			}

			return zA - zB;
		}
	}

	SortedSet<Entity> entities;

	public DrawOrderManager() {
		super();
		entities = new TreeSet<Entity>(new ZOrderComperator());
	}

	/**
	 * Add a list of entities to a list sorted on z-index
	 * 
	 * @param Collection
	 *            of entities to be added
	 */
	public void add(final Collection<Entity> entitiesList) {
		for (final Entity en : entitiesList) {
			if (en.hasAttribute(Attribute.Z_INDEX)) {
				entities.add(en);
			}
		}
	}

	/**
	 * Add entity to a list sorted on z-index
	 * 
	 * @param e
	 *            Entity to be added
	 * @return True if added, false if not
	 */
	public boolean add(final Entity e) {
		if (!e.hasAttribute(Attribute.Z_INDEX)) {
			return false;
		}
		return entities.add(e);
	}

	public SortedSet<Entity> getList() {
		return entities;
	}

	public void draw(final Renderer renderer) {
		/* Draw all entities in the correct order */
		for (final Entity ent : entities) {
			ent.sendMessage(MessageType.RENDER, renderer);
		}
	}

	public void removeEntity(final Entity entity) {
		entities.remove(entity);
	}
}
