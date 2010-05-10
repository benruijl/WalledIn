package walledin.game.entity.behaviors;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerAnimationBehavior extends AnimationBehavior {
	private float walkAnimFrame;
	private final float animSpeed;

	public PlayerAnimationBehavior(final Entity owner) {
		super(owner);
		setAttribute(Attribute.WALK_ANIM_FRAME, new Float(0));
		animSpeed = 0.6f;
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.WALKED) {
			walkAnimFrame += animSpeed;
			walkAnimFrame %= 2 * Math.PI;
			setAttribute(Attribute.WALK_ANIM_FRAME, new Float(walkAnimFrame));
		}

	}

}
