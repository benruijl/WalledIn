package walledin.engine.math;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.max.MaxCore;

public class Polygon2f {
    private List<Line2f> edges;
    private Vector2f position;

    public Polygon2f(Vector2f position) {
        edges = new ArrayList<Line2f>();
        this.position = position;
    }

    public Polygon2f(List<Line2f> edges, Vector2f position) {
        super();
        this.edges = edges;
        this.position = position;
    }

    public List<Line2f> getEdges() {
        return edges;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setEdges(List<Line2f> edges) {
        this.edges = edges;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    /**
     * Returns time of collision.
     * 
     * @param circle
     *            Circle
     * @param velocity
     *            Velocity
     * @return Time. Negative means no collision.
     */
    public float circleCollision(Circle circle, Vector2f velocity) {
        float collisionTime = Float.MAX_VALUE;
        boolean collision = false;

        for (Line2f edge : edges) {
            float time = edge.circleLineCollision(circle, velocity);

            if (time >= 0 && time < collisionTime) {
                collision = true;
                collisionTime = time;
            }
        }

        return collision ? collisionTime : -1.0f;
    }
}
