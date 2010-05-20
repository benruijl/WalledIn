package walledin.game.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Entity {
	private final static Logger LOG = Logger.getLogger(Entity.class.getName());
	private final Map<Class<? extends Behavior>, Behavior> behaviors;
	private final Map<Attribute, Object> attributes;
	private Set<Attribute> changedAttributes;
	private final String name;
	private final String familyName;
	private boolean markedRemoved;

	/**
	 * 
	 * @param name
	 *            Name of the component
	 */
	public Entity(final String name, final String familyName) {
		behaviors = new HashMap<Class<? extends Behavior>, Behavior>();
		attributes = new HashMap<Attribute, Object>();
		changedAttributes = new HashSet<Attribute>();
		this.name = name;
		this.familyName = familyName;
		markedRemoved = false;
	}

	/**
	 * Get the name of the entity
	 * 
	 * @return Name of the entity
	 */
	public String getName() {
		return name;
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
	public void addBehavior(final Behavior behavior) {
		final Class<? extends Behavior> clazz = behavior.getClass();
		if (behaviors.containsKey(clazz)) {
			throw new IllegalArgumentException("Entity [" + toString()
					+ "] already contains Component of class: "
					+ behavior.getClass().getName());
		}
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
	public <T extends Behavior> T getBehavior(final Class<T> clazz) {
		if (!behaviors.containsKey(clazz)) {
			throw new IllegalArgumentException("Object " + name
					+ " does not have behaviour of class " + clazz.getName());
		}

		final T beh = (T) behaviors.get(clazz);
		return beh;
	}

	/**
	 * Checks if entity has a certain attribute. Useful in situations where you
	 * want to filter some entities.
	 * 
	 * @param attribute
	 *            The attribute to check
	 * @return True if entity has attribute, else false
	 */
	public boolean hasAttribute(final Attribute attribute) {
		return attributes.containsKey(attribute);
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
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(final Attribute attribute) {
		if (!attributes.containsKey(attribute)) {
			throw new IllegalArgumentException("Object " + name
					+ " does not have attribute " + attribute.name());
		}

		final T att = (T) attributes.get(attribute);
		return att;
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
	@SuppressWarnings("unchecked")
	public <T> T setAttribute(final Attribute attribute, final T newObject) {
		if (attribute.clazz.isInstance(newObject)) {
			if (newObject == null) {
				LOG.warning("Storing null value for attribute " + attribute
						+ " of entity " + name);
			}
			final T result = (T) attributes.put(attribute, newObject);
			if (attribute.sendOverNetwork) {
				changedAttributes.add(attribute);
			}
			sendMessage(MessageType.ATTRIBUTE_SET, attribute);
			return result;
		} else {
			throw new IllegalArgumentException("Object should be of class: "
					+ attribute.clazz.getName());
		}
	}

	/**
	 * Get attributes that can be send over the network that have been changed
	 * since the last call
	 * 
	 * @return The changed attributes
	 */
	public Map<Attribute, Object> getChangedAttributes() {
		Map<Attribute, Object> temp = new HashMap<Attribute, Object>();
		for (Attribute attribute : changedAttributes) {
			temp.put(attribute, attributes.get(attribute));
		}
		changedAttributes = new HashSet<Attribute>();
		return temp;
	}

	/**
	 * Gets the contents of all the network attributes
	 * 
	 * @return
	 */
	public Map<Attribute, Object> getNetworkAttributes() {
		Map<Attribute, Object> temp = new HashMap<Attribute, Object>();
		for (Attribute attribute : attributes.keySet()) {
			if (attribute.sendOverNetwork) {
				temp.put(attribute, attributes.get(attribute));
			}
		}
		return temp;
	}

	/**
	 * Removes a behavior from this entity
	 * 
	 * @param clazz
	 *            Class of the behavior to be removed
	 * @return The behavior that was removed
	 */
	public Behavior removeBehavior(final Class<? extends Behavior> clazz) {
		final Behavior behavior = behaviors.remove(clazz);
		return behavior;
	}

	/**
	 * Calls onMessage on all the behaviors of this entity
	 */
	public void sendMessage(final MessageType messageType, final Object data) {
		for (final Behavior behavior : behaviors.values()) {
			behavior.onMessage(messageType, data);
		}
	}

	/**
	 * Calls onUpdate on all the behaviors of this entity
	 */
	public void sendUpdate(final double delta) {
		for (final Behavior behavior : behaviors.values()) {
			behavior.onUpdate(delta);
		}
	}

	public String getFamilyName() {
		return familyName;
	}

	/**
	 * Mark this entity for removal
	 */
	public void remove() {
		markedRemoved = true;
	}

	public boolean isMarkedRemoved() {
		return markedRemoved;
	}
}
