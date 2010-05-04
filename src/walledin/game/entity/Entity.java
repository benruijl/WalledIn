package walledin.game.entity;

import java.util.HashMap;
import java.util.Map;

import walledin.game.components.Behavior;
import walledin.game.components.Message;

public class Entity {
	private final Map<Class<? extends Behavior>, Behavior> behaviors;
	private final String name;

	/**
	 * 
	 * @param name
	 *            Name of the component
	 */
	public Entity(String name) {
		behaviors = new HashMap<Class<? extends Behavior>, Behavior>();
		this.name = name;
	}

	/**
	 * Adds a behavior to this class
	 * 
	 * @param behavior
	 *            The behavior instance to be added
	 * @throws IllegalArgumentException
	 *             Thrown when the entity already contains an behavior of this
	 *             type
	 */
	public void addBehavior(Behavior behavior) {
		Class<? extends Behavior> clazz = behavior.getClass();
		if (behaviors.containsKey(clazz)) {
			throw new IllegalArgumentException("Entity [" + toString()
					+ "] already contains Component of class: "
					+ behavior.getClass().getName());
		}
		behavior.setOwner(this);
		behaviors.put(clazz, behavior);
	}

	/**
	 * Retrieves a behavior of this entity based on the class of the behavior
	 * 
	 * @param <T>
	 *            The type of the Behavior that has to be fetched
	 * @param clazz
	 *            Class of the Behavior that has to be fetched
	 * @return The requested behavior or null if this entity doesn't have a
	 *         behavior of this class
	 */
	@SuppressWarnings("unchecked")
	public <T extends Behavior> T getBehavior(Class<T> clazz) {
		return (T) behaviors.get(clazz);
	}

	/**
	 * Removes a behavior from this entity
	 * 
	 * @param clazz
	 *            Class of the behavior to be removed
	 * @return The behavior that was removed
	 */
	public Behavior removeBehavior(Class<? extends Behavior> clazz) {
		Behavior behavior = behaviors.remove(clazz);
		behavior.detachFromOwner();
		return behavior;
	}

	/**
	 * Calls onMessage on all the behaviors of this entity
	 */
	public void onMessage(Message message, Object data) {
		for (Behavior behavior : behaviors.values()) {
			behavior.onMessage(message, data);
		}
	}

	/**
	 * Calls onUpdate on all the behaviors of this entity
	 */
	public void onUpdate(double delta) {
		for (Behavior behavior : behaviors.values()) {
			behavior.onUpdate(delta);
		}
	}
}
