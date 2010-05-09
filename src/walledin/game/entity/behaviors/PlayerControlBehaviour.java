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
	
	
	public PlayerControlBehaviour(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onUpdate(double delta) {
		Vector2f velocity = new Vector2f(GRAVITY); // do gravity

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT)) {
			velocity.x += MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, new Integer(1));
			getOwner().sendMessage(MessageType.WALKED, null);
			
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT)) {
			velocity.x -= MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, new Integer(-1));
			getOwner().sendMessage(MessageType.WALKED, null);
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
