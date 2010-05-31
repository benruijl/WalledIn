package walledin.game.entity.behaviors;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerParentBehavior extends Behavior {
	
	public PlayerParentBehavior(Entity owner) {
		super(owner);
	}

	/**
	 * This function updates the children of the player (think
	 * of weapons) with the current player position, orientation etc.
	 */
	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType != MessageType.ATTRIBUTE_SET)
			return;
		
		if (getOwner().hasAttribute(Attribute.WEAPON) == false)
			return;
		
		final Entity weapon = (Entity) getAttribute(Attribute.WEAPON);
		final Attribute attrib = (Attribute) data;
		final int or = (Integer) getAttribute(Attribute.ORIENTATION);
		final Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
		final Vector2f vel = (Vector2f) getAttribute(Attribute.VELOCITY);
		
			switch (attrib) {
			case POSITION:
				weapon.setAttribute(Attribute.POSITION, pos.add(new Vector2f(35.0f, 20.0f)));
				break;
			case VELOCITY:
				weapon.setAttribute(Attribute.VELOCITY, vel);
				break;
			case ORIENTATION:
				weapon.setAttribute(Attribute.ORIENTATION, or);
				break;
			}

	}

	@Override
	public void onUpdate(double delta) {
	}
}
