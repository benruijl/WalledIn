package walledin.game.entity.behaviors.physics;

import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
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
        // Set the colliding object back to the original position for now
        // and half the speed
        final Vector2f velB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.VELOCITY)).scale((float) data
                .getDelta());

        final Vector2f oldPosB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION)).sub(velB);

        data.getCollisionEntity().setAttribute(Attribute.POSITION, oldPosB);
        data.getCollisionEntity().setAttribute(
                Attribute.VELOCITY,
                ((Vector2f) data.getCollisionEntity().getAttribute(
                        Attribute.VELOCITY)).scale(0.5f));

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
