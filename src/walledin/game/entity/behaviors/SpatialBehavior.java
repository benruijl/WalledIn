package walledin.game.entity.behaviors;

import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.math.Circle;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class SpatialBehavior extends Behavior {
	private Vector2f position;
	private Vector2f velocity;
	private Rectangle boundingBox;
	private Circle boundingCircle;

	public SpatialBehavior(final Entity owner, final Vector2f position,
			final Vector2f velocity) {
		super(owner);
		this.position = position;
		this.velocity = velocity;
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
				// recreate circle
				boundingCircle = Circle.fromRect(boundingBox);
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
