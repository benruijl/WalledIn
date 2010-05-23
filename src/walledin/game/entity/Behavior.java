/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

*/
package walledin.game.entity;

/**
 * Base behavior class. Subclasses define specific behavior of its owner.
 * 
 * @author wouter
 * 
 */
public abstract class Behavior {
	private final Entity owner;

	public Behavior(final Entity owner) {
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
	protected <T> T getAttribute(final Attribute attribute) {
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
	protected <T> T setAttribute(final Attribute attribute, final T newObject) {
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
