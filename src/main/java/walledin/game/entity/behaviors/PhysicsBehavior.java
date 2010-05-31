package walledin.game.entity.behaviors;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PhysicsBehavior extends Behavior {
	private final Vector2f gravity = new Vector2f(0, 0.00001f); // acceleration of gravity
	private Vector2f acceleration = new Vector2f(0, 0);
	
	public PhysicsBehavior(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.APPLY_FORCE)
		{
			acceleration.add((Vector2f) data);
		}

	}

	@Override
	public void onUpdate(double delta) {
		Vector2f velCur = (Vector2f) getAttribute(Attribute.VELOCITY);
		Vector2f posCur = (Vector2f) getAttribute(Attribute.POSITION);
		
		acceleration = acceleration.add(gravity);
		
		Vector2f velNew = velCur.add(acceleration.scale((float) delta));
		Vector2f posNew = posCur.add(velNew.scale((float) delta));

		setAttribute(Attribute.VELOCITY, velNew);
		setAttribute(Attribute.POSITION, posNew);
		
		acceleration = new Vector2f(0, 0);
	}

}
