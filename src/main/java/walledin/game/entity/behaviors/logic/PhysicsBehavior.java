package walledin.game.entity.behaviors.logic;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

public class PhysicsBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
    private static final Vector2f GRAVITY = new Vector2f(0, 40.0f);
    private RigidBody body;
    private boolean doGravity = true;
    private boolean internalUpdate = false;

    public PhysicsBehavior(Entity owner, RigidBody body) {
        super(owner);
        this.body = body;
    }

    public PhysicsBehavior(Entity owner, RigidBody body, boolean doGravity) {
        super(owner);
        this.body = body;
        this.doGravity = doGravity;
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        if (body == null) {
            return;
        }

        /* FIXME: wrong message name! */
        if (messageType == MessageType.APPLY_FORCE) {
            Vector2f imp = (Vector2f) data;
            applyForce(imp);
        }

        if (messageType == MessageType.ATTRIBUTE_SET) {
            if (data == Attribute.POSITION) {
                if (!internalUpdate) {
                    Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                            .asRectangle();

                    Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
                    pos = pos.add(new Vector2f(rect.getWidth() / 2.0f, rect
                            .getHeight() / 2.0f));

                    Quat4f rot = new Quat4f();

                    if (getOwner().hasAttribute(Attribute.ORIENTATION_ANGLE)) {
                        final float angle = (Float) getAttribute(Attribute.ORIENTATION_ANGLE);
                        QuaternionUtil.setRotation(rot, new Vector3f(0, 0, 1),
                                angle);
                    } else {
                        body.getOrientation(rot);
                    }

                    body.setWorldTransform(new Transform(new Matrix4f(rot,
                            new Vector3f(pos.getX(), pos.getY(), 0), 1)));
                }
            }
        }
    }

    private void applyForce(Vector2f imp) {
        // Apply the force at the center of the mass
        body.applyForce(new Vector3f(imp.getX(), imp.getY(), 0),
                new Vector3f());
    }

    @Override
    public void onUpdate(double delta) {
        if (body != null) {

            if (doGravity) {
                applyForce(GRAVITY);
            }

            /* Correct the position. */
            Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                    .asRectangle();

            Vector3f pos3D = body.getCenterOfMassPosition(new Vector3f());
            Vector3f vel3D = body.getLinearVelocity(new Vector3f());
            Quat4f angleQuat = body.getOrientation(new Quat4f());

            Vector2f pos = new Vector2f(pos3D.x - rect.getWidth() / 2.0f,
                    pos3D.y - rect.getHeight() / 2.0f);

            internalUpdate = true;
            setAttribute(Attribute.POSITION, pos);
            setAttribute(Attribute.VELOCITY, new Vector2f(vel3D.x, vel3D.y));
            // setAttribute(Attribute.ORIENTATION_ANGLE, QuaternionUtil
            // .getAngle(angleQuat));
            internalUpdate = false;
        }
    }
}
