package walledin.game.entity.behaviors;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class BulletBehavior extends Behavior {

	public BulletBehavior(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.COLLIDED) {
			final CollisionData colData = (CollisionData) data;
			
			// if collided with map, destroy
			if (colData.getCollisionEntity().hasAttribute(Attribute.TILES)) 
				getOwner().remove();
		}

	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
