package walledin.game.entity.behaviors;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class SpatialBehavior extends Behavior {
	private Vector2f position;
	private Vector2f velocity;

	public SpatialBehavior(final Entity owner) {
		super(owner);
		setAttribute(Attribute.POSITION, new Vector2f()); // create attribute
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.ATTRIBUTE_SET) {
			final Attribute attribute = (Attribute) data;
			switch (attribute) {
			case POSITION:
				position = getAttribute(attribute);
				break;
			case VELOCITY:
				velocity = getAttribute(attribute);
				break;
			}
		}
	}

	@Override
	public void onUpdate(final double delta) {
		Vector2f scaledVelocity = new Vector2f(velocity);
		scaledVelocity = scaledVelocity.scale((float) delta);
		position = position.add(scaledVelocity);
		setAttribute(Attribute.POSITION, position);
	}

	public Vector2f getPosition() {
		return position;
	}

}
