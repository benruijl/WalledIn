package walledin.game.entity.behaviors;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthKitBehavior extends Behavior {
	private int strength;

	public HealthKitBehavior(Entity owner, int strength) {
		super(owner);

		this.strength = strength;
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.COLLIDED)
		{
			// assumes colliding entity has a health component
			CollisionData colData = (CollisionData)data;
			System.out.println(getOwner().getName() + " collided with " + colData.getCollisionEntity().getName());
			colData.getCollisionEntity().sendMessage(MessageType.RESTORE_HEALTH, Integer.valueOf(strength));
		}
	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
