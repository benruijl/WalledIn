package walledin.game.entity.behaviors.logic;

import org.apache.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.engine.physics.PhysicsBody;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PhysicsBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
    private static final Vector2f GRAVITY = new Vector2f(0, 4000.0f);
    private PhysicsBody body;
    private boolean doGravity = true;

    public PhysicsBehavior(Entity owner, Body body) {
        super(owner);
        this.body = new PhysicsBody(body);
    }

    public PhysicsBehavior(Entity owner, Body body, boolean doGravity) {
        super(owner);
        this.body = new PhysicsBody(body);
        this.doGravity = doGravity;
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        /* FIXME: wrong message name! */
        if (messageType == MessageType.APPLY_FORCE) {
            body.applyImpulse((Vector2f) data);
        }

        if (messageType == MessageType.ATTRIBUTE_SET) {
            if (data == Attribute.POSITION) {
                Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                        .asRectangle();
                Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
                pos = pos.add(new Vector2f(rect.getWidth() / 2.0f, rect
                        .getHeight() / 2.0f));

                /* Be careful calling this function! */
                body.getBody().setXForm(new Vec2(pos.getX(), pos.getY()),
                        body.getBody().getAngle());
            }
        }
    }

    @Override
    public void onUpdate(double delta) {
        if (body != null) {

            if (doGravity) {
                body.applyImpulse(GRAVITY);
            }

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
