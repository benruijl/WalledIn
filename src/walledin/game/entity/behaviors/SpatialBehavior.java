package walledin.game.entity.behaviors;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class SpatialBehavior extends Behavior {
	private Vector2f position;
	private Vector2f velocity;
	private Rectangle boundingBox;
	private Circle boundingCircle;

	public SpatialBehavior(final Entity owner) {
		super(owner);
		position = new Vector2f();
		velocity = new Vector2f();
		boundingBox = new Rectangle();
		boundingCircle = new Circle();

		setAttribute(Attribute.POSITION, position); // create attribute
		setAttribute(Attribute.VELOCITY, velocity);
		setAttribute(Attribute.BOUNDING_RECT, boundingBox);
		setAttribute(Attribute.BOUNDING_CIRCLE, boundingCircle);
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

			case BOUNDING_RECT:
				boundingBox = getAttribute(attribute);
				boundingCircle = Circle.fromRect(boundingBox); // recreate
																// circle
				setAttribute(Attribute.BOUNDING_CIRCLE, boundingCircle);
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
