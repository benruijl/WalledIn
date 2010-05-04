package walledin.game.entity.behaviors;

import java.awt.event.KeyEvent;

import walledin.engine.Input;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;

public class PlayerControlBehaviour extends SpatialBehavior {
	private static final Vector2f GRAVITY = new Vector2f(0, 2.0f);
	private static final float MOVE_SPEED = 4.0f;
	@Override
	public void onUpdate(double delta) {
		Vector2f vNewPos = getAttribute(Attribute.POSITION);

		vNewPos = vNewPos.add(GRAVITY); // do gravity

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT)) {
			vNewPos.x += MOVE_SPEED;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT)) {
			vNewPos.x -= MOVE_SPEED;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_UP)) {
			vNewPos.y -= MOVE_SPEED;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_DOWN)) {
			vNewPos.y += MOVE_SPEED;
		}
		
		setAttribute(Attribute.POSITION, vNewPos);
	}
}
