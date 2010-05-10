package walledin.game.entity.behaviors;

import java.awt.event.KeyEvent;

import walledin.engine.Input;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends SpatialBehavior {
	private static final Vector2f GRAVITY = new Vector2f(0, 40.0f);
	private static final float MOVE_SPEED = 80.0f;

	public PlayerControlBehaviour(final Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onUpdate(final double delta) {
		Vector2f velocity = new Vector2f(GRAVITY); // do gravity

		float x = 0;
		float y = 0;

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT)) {
			x += MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, new Integer(1));
			getOwner().sendMessage(MessageType.WALKED, null);

		}
		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT)) {
			x -= MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, new Integer(-1));
			getOwner().sendMessage(MessageType.WALKED, null);
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_UP)) {
			y -= MOVE_SPEED;
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_DOWN)) {
			y += MOVE_SPEED;
		}

		velocity = velocity.add(new Vector2f(x, y));

		setAttribute(Attribute.VELOCITY, velocity);
		super.onUpdate(delta);
	}
}
