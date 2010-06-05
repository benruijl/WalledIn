package walledin.game.entity.behaviors.logic;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class FoamBulletBehavior extends BulletBehavior {
    private boolean blownUp;

    public FoamBulletBehavior(final Entity owner) {
        super(owner, 0);
        blownUp = false;
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        super.onMessage(messageType, data);
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;
            if (!blownUp) {

                // if collided with map or other foampartical create a
                // foampartical
                if (colData.getCollisionEntity().hasAttribute(Attribute.TILES)
                        || colData.getCollisionEntity().getFamilyName()
                                .equals("foampartical")) {
                    final EntityManager manager = getEntityManager();
                    final Entity partical = manager.create("foampartical",
                            manager.generateUniqueName("foampartical"));
                    partical.setAttribute(Attribute.POSITION,
                            getAttribute(Attribute.POSITION));
                    blownUp = true;
                }
            }
        }
    }
}
