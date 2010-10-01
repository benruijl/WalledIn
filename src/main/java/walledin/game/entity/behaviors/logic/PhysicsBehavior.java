package walledin.game.entity.behaviors.logic;

import org.apache.log4j.Logger;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.engine.physics.PhysicsBody;
import walledin.engine.physics.PhysicsManager;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PhysicsBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
    private PhysicsBody body;

    public PhysicsBehavior(Entity owner) {
        super(owner);
        body = null;
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        /* FIXME: wrong message name! */
        if (messageType == MessageType.APPLY_FORCE) {
            body.applyImpulse((Vector2f) data);
        }

        /* When the position is set, create a body. */
        if (messageType == MessageType.ATTRIBUTE_SET) {
            if (data == Attribute.POSITION) {
                if (body == null) {
                    body = PhysicsManager
                            .getInstance()
                            .addBody(
                                    ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                                            .asRectangle()
                                            .translate(
                                                    (Vector2f) getAttribute(Attribute.POSITION)),
                                    getOwner().getName());
                }
            }
        }
    }

    @Override
    public void onUpdate(double delta) {
        if (body != null) {
            /* Correct the position. */
            Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                    .asRectangle();
            Vector2f pos = new Vector2f(body.getPosition().getX()
                    - rect.getWidth() / 2.0f, body.getPosition().getY()
                    - rect.getHeight() / 2.0f);
            setAttribute(Attribute.POSITION, pos);
            setAttribute(Attribute.VELOCITY, body.getVelocity());
        }
    }
}
