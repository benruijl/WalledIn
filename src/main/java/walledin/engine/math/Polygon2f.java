package walledin.engine.math;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.max.MaxCore;

import com.sun.org.apache.bcel.internal.generic.FLOAD;

import walledin.game.CollisionManager.GeometricalCollisionData;

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

        /* Check if it is already colliding. */
        Vector2f closest = closestPointOnPolygon(circle.getPos());
        float t = circle.pointCollision(closest, velocity);
        if (t >= 0) {
            return t;
        }

        for (Line2f edge : edges) {
            float time = edge.circleLineCollision(circle, velocity);

            if (time >= 0 && time < collisionTime) {
                collision = true;
                collisionTime = time;
            }
        }

        return collision ? collisionTime : -1.0f;
    }

    public Vector2f closestPointOnPolygon(Vector2f point) {
        Vector2f closest = new Vector2f();
        float distance = Float.MAX_VALUE;

        for (Line2f edge : edges) {
            Vector2f closestToEdge = edge.projectionPointToLine(point);
            float distSquared = point.sub(closestToEdge).lengthSquared();
            if (distSquared < distance) {
                distance = distSquared;
                closest = closestToEdge;
            }
        }

        return closest;
    }

    public GeometricalCollisionData circleCollisionData(Circle circle,
            Vector2f velocity) {
        float time = circleCollision(circle, velocity);

        if (time < 0) {
            return new GeometricalCollisionData(false, 0, null, null);
        }

        Vector2f circlePos = circle.getPos().add(velocity.scale(time));
        Vector2f polygonPoint = closestPointOnPolygon(circlePos);
        Vector2f circlePoint = new Circle(circlePos, circle.getRadius())
                .closestPointOnCircle(polygonPoint);

        Vector2f normal = circlePos.sub(polygonPoint).normalize();
        Vector2f penetration = polygonPoint.sub(circlePoint);

        return new GeometricalCollisionData(true, time, normal, penetration);
    }
}
