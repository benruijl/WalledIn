package walledin.game.entity;


/**
 * Base behavior class. Subclasses define specifiek behavior of its owner.
 * 
 * @author wouter
 * 
 */
public abstract class Behavior {
	private Entity owner;

	/**
	 * Sets the owner of the entity. This should only be called by the Entity
	 * class
	 * 
	 * @param owner
	 *            The entity that owns this behavior instance
	 * @throws IllegalArgumentException
	 *             thrown when the behavior is already owned by another entity
	 */
	public void setOwner(Entity owner) {
		if (this.owner != null) {
			throw new IllegalArgumentException(
					"Cannot set owner before detaching");
		}
		this.owner = owner;
	}

	/**
	 * Detaches from owner. This method should only be called from the Entity
	 * class
	 */
	public void detachFromOwner() {
		this.owner = null;
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
	 * Sends a message to this behavior. Subclasses should react to certain
	 * messages.
	 * 
	 * @param messageType
	 *            Enum that specifies the message type
	 * @param data
	 *            Message specific data
	 */
	public void onMessage(MessageType messageType, Object data) {

	}

	/**
	 * Called when the engine request an update in the status.
	 * 
	 * @param delta
	 *            Amount of time elapsed since last update
	 */
	public void onUpdate(double delta) {

	}
}
