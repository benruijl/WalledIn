package walledin.game.entity;

/**
 * Base behavior class. Subclasses define specific behavior of its owner.
 * 
 * @author wouter
 * 
 */
public abstract class Behavior {
	private Entity owner;
	
	public Behavior(Entity owner)
	{
		this.owner = owner;
	}

	/**
	 * Returns the current owner
	 * 
	 * @return The owner
	 */
	public Entity getOwner() {
		return owner;
	}

	/**
	 * Gets the object bound to the attribute. Performs an automatic cast, so
	 * the user doesn't have to cast every time. The attribute is owned by the
	 * owner of this behavior.
	 * 
	 * @param attribute
	 *            The attribute to get
	 * @return Returns the object bound to this attribute
	 * @throws ClassCastException
	 *             if the class you asked for is not the class specified in
	 *             attribute
	 */
	protected <T> T getAttribute(Attribute attribute) {
		return getOwner().getAttribute(attribute);
	}

	/**
	 * Binds the object to the attribute and returns the old object. The
	 * attribute is owned by the owner of this behavior.
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
	protected <T> T setAttribute(Attribute attribute, T newObject) {
		return getOwner().setAttribute(attribute, newObject);
	}

	/**
	 * Sends a message to this behavior. Subclasses should react to certain
	 * messages.
	 * 
	 * @param messageType
	 *            Enum that specifies the message type
	 * @param data
	 *            Message specific data
	 */
	public abstract void onMessage(MessageType messageType, Object data);

	/**
	 * Called when the engine request an update in the status.
	 * 
	 * @param delta
	 *            Amount of time elapsed since last update
	 */
	public abstract void onUpdate(double delta);
}
