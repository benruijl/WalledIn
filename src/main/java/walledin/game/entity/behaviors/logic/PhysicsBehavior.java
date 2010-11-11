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
import com.bulletphysics.linearmath.Transform;

public class PhysicsBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
    private static final Vector3f GRAVITY = new Vector3f(0, 40.0f, 0);
    private RigidBody body;
    private boolean doGravity = true;

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
            body.applyImpulse(new Vector3f(imp.getX(), imp.getY(), 0),
                    new Vector3f());
        }

        if (messageType == MessageType.ATTRIBUTE_SET) {
            if (data == Attribute.POSITION) {
                Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                        .asRectangle();
                Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
                pos = pos.add(new Vector2f(rect.getWidth() / 2.0f, rect
                        .getHeight() / 2.0f));

                body.setWorldTransform(new Transform(new Matrix4f(body
                        .getOrientation(new Quat4f(0, 0, 0, 1)), new Vector3f(
                        pos.getX(), pos.getY(), 0), 1)));
            }
        }
    }

    @Override
    public void onUpdate(double delta) {
        if (body != null) {

            if (doGravity) {
                body.applyImpulse(GRAVITY, new Vector3f()); // apply at center
                                                            // of mass
            }

            /* Correct the position. */
            Rectangle rect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                    .asRectangle();

            Vector3f pos3D = body.getCenterOfMassPosition(new Vector3f());
            Vector3f vel3D = body.getLinearVelocity(new Vector3f());

            // reset velocity
            body.setLinearVelocity(new Vector3f(vel3D.x, vel3D.y, 0));

            Vector2f pos = new Vector2f(pos3D.x - rect.getWidth() / 2.0f,
                    pos3D.y - rect.getHeight() / 2.0f);
            setAttribute(Attribute.POSITION, pos);
            setAttribute(Attribute.VELOCITY, new Vector2f(vel3D.x, vel3D.y));
        }
    }
}
