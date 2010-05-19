package walledin.game.entity.behaviors;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthKitBehavior extends Behavior {
	private final int strength;

	public HealthKitBehavior(final Entity owner, final int strength) {
		super(owner);

		this.strength = strength;
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.COLLIDED) {
			// assumes colliding entity has a health component
			final CollisionData colData = (CollisionData) data;
			System.out.println(getOwner().getName() + " collided with "
					+ colData.getCollisionEntity().getName());
			colData.getCollisionEntity().sendMessage(
					MessageType.RESTORE_HEALTH, Integer.valueOf(strength));
			
			getOwner().remove(); // remove after usage
		}
	}

	@Override
	public void onUpdate(final double delta) {
		// TODO Auto-generated method stub

	}

}
