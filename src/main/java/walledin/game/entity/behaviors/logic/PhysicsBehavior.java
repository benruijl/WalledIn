package walledin.game.entity.behaviors.logic;

import org.apache.log4j.Logger;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Circle;
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
    private boolean isStatic = false;
    private boolean isCircle = false;

    public PhysicsBehavior(Entity owner) {
        super(owner);
        body = null;
    }

    public PhysicsBehavior(Entity owner, boolean isStatic) {
        super(owner);
        body = null;
        this.isStatic = isStatic;
    }

    /*
     * public PhysicsBehavior(Entity owner, boolean isStatic, boolean isCircle)
     * { super(owner); body = null; this.isStatic = isStatic; this.isCircle =
     * isCircle; }
     */

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
                    String name = getOwner().getName();
                    Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
                    AbstractGeometry geom = (AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY);

                    if (geom instanceof Rectangle) {
                        if (isStatic) {
                            body = PhysicsManager.getInstance().addStaticBody(
                                    geom.asRectangle().translate(pos), name);
                        } else {
                            body = PhysicsManager.getInstance().addBody(
                                    geom.asRectangle().translate(pos), name);
                        }
                    } else if (geom instanceof Circle) {
                        if (isStatic) {
                            body = PhysicsManager.getInstance()
                                    .addStaticCircleBody(
                                            geom.asCircumscribedCircle()
                                                    .translate(pos), name);
                        } else {
                            body = PhysicsManager.getInstance()
                                    .addCircleBody(
                                            geom.asCircumscribedCircle()
                                                    .translate(pos), name);
                        }
                    }
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
