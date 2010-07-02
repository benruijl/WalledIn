package walledin.game.entity.behaviors.physics;

import walledin.engine.math.Geometry;
import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

/**
 * This is handles the collision response for objects that are immovable. Other
 * objects colliding with it will not fly through, but will be stopped.
 * 
 * @author Ben Ruijl
 * 
 */
public class StaticObjectCollisionReponse extends Behavior {

    public StaticObjectCollisionReponse(Entity owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    void doResponse(CollisionData data) {

        if (data.getCollisionEntity().getFamily() == Family.MAP) {
            return;
        }

        final Vector2f velB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.VELOCITY)).scale((float) data
                .getDelta());

        final Vector2f endPosB = (Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION);
        final Vector2f oldPosB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION)).sub(velB);

        Geometry boundsA = (Geometry) getAttribute(Attribute.BOUNDING_GEOMETRY);
        Geometry boundsB = (Geometry) data.getCollisionEntity().getAttribute(
                Attribute.BOUNDING_GEOMETRY);

        boundsA = boundsA
                .translate((Vector2f) getAttribute(Attribute.POSITION));
        boundsB = boundsB.translate((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION));

        /* Do a binary search to resolve the collision */
        int maxDepth = 3;
        Vector2f left = oldPosB;
        Vector2f right = endPosB;
        int depth = 0;
        while (depth < maxDepth) {
            Vector2f mid = left.add(right.sub(left).scale(0.5f));

            if (boundsB.translate(mid).intersects(boundsA)) {
                right = mid;
            } else {
                left = mid;
            }
            depth++;
        }

        Vector2f resolvedPos = left;//.add(right.sub(left).scale(0.5f));

        data.getCollisionEntity().setAttribute(Attribute.POSITION, resolvedPos);
        data.getCollisionEntity().setAttribute(Attribute.VELOCITY,
                resolvedPos.sub(oldPosB).scale(1 / (float) data.getDelta()));

    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        if (messageType == MessageType.COLLIDED) {
            doResponse((CollisionData) data);
        }
    }

    @Override
    public void onUpdate(double delta) {
        // TODO Auto-generated method stub

    }

}
