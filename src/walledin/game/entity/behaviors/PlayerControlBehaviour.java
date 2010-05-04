package walledin.game.entity.behaviors;

import java.awt.event.KeyEvent;

import walledin.engine.Input;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;

public class PlayerControlBehaviour extends SpatialBehavior {
	private static final Vector2f GRAVITY = new Vector2f(0, 40.0f);
	private static final float MOVE_SPEED = 80.0f;
	@Override
	public void onUpdate(double delta) {
		Vector2f velocity = new Vector2f(GRAVITY); // do gravity

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT)) {
			velocity.x += MOVE_SPEED;
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT)) {
			velocity.x -= MOVE_SPEED;
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_UP)) {
			velocity.y -= MOVE_SPEED;
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_DOWN)) {
			velocity.y += MOVE_SPEED;
		}
		
		setAttribute(Attribute.VELOCITY, velocity);
		super.onUpdate(delta);
	}
}
