package walledin.engine.physics;

import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.engine.Renderer.ColorRGB;
import walledin.engine.math.Vector2f;

import com.bulletphysics.linearmath.IDebugDraw;

/**
 * Visualizes the physics on the screen.
 * 
 * @author Ben Ruijl
 * 
 */
public class DebugDrawer extends IDebugDraw {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(DebugDrawer.class);

    private final Renderer renderer;
    private int debugMode;

    public DebugDrawer(Renderer renderer) {
        super();
        this.renderer = renderer;
    }

    @Override
    public void draw3dText(Vector3f location, String textString) {
        // TODO: implement
    }

    @Override
    public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB,
            float distance, int lifeTime, Vector3f color) {
        // TODO: implement
    }

    @Override
    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        renderer.drawLineSegment(new Vector2f(from.x, from.y), new Vector2f(
                to.x, to.y), new ColorRGB(color.x, color.y, color.z));
    }

    @Override
    public int getDebugMode() {
        return debugMode;
    }

    @Override
    public void reportErrorWarning(String warningString) {
        LOG.info(warningString);
    }

    @Override
    public void setDebugMode(int debugMode) {
        this.debugMode = debugMode;
    }

}
