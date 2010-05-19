package walledin.game.entity.behaviors;

import java.awt.event.KeyEvent;

import walledin.engine.Input;
import walledin.engine.math.Vector2f;
import walledin.game.Item;
import walledin.game.ItemFactory;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends SpatialBehavior {
	private static final Vector2f GRAVITY = new Vector2f(0, 100.0f);
	private static final float MOVE_SPEED = 140.0f;
	private static final float JUMP_SPEED = 3500.0f;
	private boolean canJump;

	public PlayerControlBehaviour(final Entity owner, final Vector2f position,
			final Vector2f velocity) {
		super(owner, position, velocity);
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.COLLIDED) {
			final CollisionData colData = (CollisionData) data;
			canJump = colData.getNewPos().getY() < colData.getTheorPos().getY();
		}

		super.onMessage(messageType, data);
	}

	@Override
	public void onUpdate(final double delta) {
		Vector2f velocity = new Vector2f(GRAVITY); // do gravity

		float x = 0;
		float y = 0;

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT) ||Input.getInstance().keyDown(KeyEvent.VK_D) ) {
			x += MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, 1);
			getOwner().sendMessage(MessageType.WALKED, null);

		}
		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT) ||Input.getInstance().keyDown(KeyEvent.VK_A)) {
			x -= MOVE_SPEED;
			setAttribute(Attribute.ORIENTATION, -1);
			getOwner().sendMessage(MessageType.WALKED, null);
		}
		if (Input.getInstance().keyDown(KeyEvent.VK_UP) ||Input.getInstance().keyDown(KeyEvent.VK_W)) {
			y -= MOVE_SPEED;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_DOWN) ||Input.getInstance().keyDown(KeyEvent.VK_S)) {
			y += MOVE_SPEED;
		}

		if (canJump && Input.getInstance().keyDown(KeyEvent.VK_SPACE)) {
			y -= JUMP_SPEED;
			canJump = false;
		}
		
		velocity = velocity.add(new Vector2f(x, y));

		setAttribute(Attribute.VELOCITY, velocity);
		super.onUpdate(delta);
	}
}
