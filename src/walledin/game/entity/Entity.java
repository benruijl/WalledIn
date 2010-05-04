package walledin.game.entity;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private final Map<Class<? extends Behavior>, Behavior> behaviors;
	private final Map<Attribute, Object> attributes;
	private final String name;

	/**
	 * 
	 * @param name
	 *            Name of the component
	 */
	public Entity(String name) {
		behaviors = new HashMap<Class<? extends Behavior>, Behavior>();
		attributes = new HashMap<Attribute, Object>();
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
	 * Gets the object bound to the attribute. Performs an automatic cast, so
	 * the user doesn't have to cast every time
	 * 
	 * @param attribute
	 *            The attribute to get
	 * @return Returns the object bound to this attribute
	 * @throws ClassCastException
	 *             if the class you asked for is not the class specified in
	 *             attribute
	 */
	public <T> T getAttribute(Attribute attribute) {
		return (T) attributes.get(attribute);
	}

	/**
	 * Binds the object to the attribute and returns the old object
	 * 
	 * @param attribute
	 *            The attribute to bind
	 * @param newObject
	 *            Object to bind
	 * @return Returns the object bound to the attribute before
	 * @throws IllegalArgumentException
	 *             if the newObect is not of the class specified in the
	 *             attribute
	 */
	public <T> T setAttribute(Attribute attribute, T newObject) {
		if (attribute.clazz.isInstance(newObject)) {
			return (T) attributes.put(attribute, newObject);
		} else {
			throw new IllegalArgumentException("Object should be of class: "
					+ attribute.clazz.getName());
		}
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
	public void onMessage(MessageType messageType, Object data) {
		for (Behavior behavior : behaviors.values()) {
			behavior.onMessage(messageType, data);
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
