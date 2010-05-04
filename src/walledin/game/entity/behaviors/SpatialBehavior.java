package walledin.game.entity.behaviors;

import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.MessageType;

public class SpatialBehavior extends Behavior {
	private Vector2f position;
	private Vector2f velocity;

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.ATTRIBUTE_SET) {
			Attribute attribute = (Attribute) data;
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
	public void onUpdate(double delta) {
		Vector2f scaledVelocity = new Vector2f(velocity);
		scaledVelocity.scale((float) delta);
		position = position.add(scaledVelocity);
		setAttribute(Attribute.POSITION, position);
	}

	public Vector2f getPosition() {
		return position;
	}

}
