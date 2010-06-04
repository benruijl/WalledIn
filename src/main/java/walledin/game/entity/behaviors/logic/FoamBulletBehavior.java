package walledin.game.entity.behaviors.logic;

import walledin.game.EntityManager;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class FoamBulletBehavior extends BulletBehavior {
	private boolean blownUp;

	public FoamBulletBehavior(Entity owner) {
		super(owner);
		blownUp = false;
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		super.onMessage(messageType, data);
		if (messageType == MessageType.COLLIDED) {
			final CollisionData colData = (CollisionData) data;
			if (!blownUp) {
				
				// if collided with map or other foam particle create a
				// foam particle
				if (colData.getCollisionEntity().hasAttribute(Attribute.TILES)
						|| colData.getCollisionEntity().getFamily()
								.equals(Family.FOAM_PARTICLE)) {
					final EntityManager manager = getEntityManager();
					final Entity partical = manager.create(Family.FOAM_PARTICLE,
							manager.generateUniqueName(Family.FOAM_PARTICLE));
					partical.setAttribute(Attribute.POSITION,
							getAttribute(Attribute.POSITION));
					blownUp = true;
				}
			}
		}
	}
}
