package walledin.game.entity.behaviors;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.network.server.Server;

public class PhysicsBehavior extends Behavior {
	private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
	private final Vector2f gravity = new Vector2f(0, 50.0f); // acceleration of
																// gravity
	private Vector2f acceleration = new Vector2f(0, 0);
	private float frictionCoefficient = 0.05f; // part of the velocity that is
												// kept

	public PhysicsBehavior(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.APPLY_FORCE) {
			acceleration = acceleration.add((Vector2f) data);
		}

	}

	@Override
	public void onUpdate(double delta) {
		Vector2f velCur = (Vector2f) getAttribute(Attribute.VELOCITY);
		Vector2f posCur = (Vector2f) getAttribute(Attribute.POSITION);

		acceleration = acceleration.add(gravity);

		if (delta > 1.0f) // FIXME: hack
			delta = 1.0f;

		// add friction
		acceleration = acceleration.add(new Vector2f(-Math.signum(velCur.x)
				* velCur.x * velCur.x * frictionCoefficient, -Math.signum(velCur.y)
				* velCur.y * velCur.y * frictionCoefficient));

		Vector2f velNew = velCur.add(acceleration.scale((float) delta));
		Vector2f posNew = posCur.add(velNew.scale((float) delta));

		setAttribute(Attribute.VELOCITY, velNew);
		setAttribute(Attribute.POSITION, posNew);

		acceleration = new Vector2f(0, 0);
	}

}
