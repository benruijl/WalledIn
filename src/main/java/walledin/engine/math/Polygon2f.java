/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.game.CollisionManager.GeometricalCollisionData;

public class Polygon2f {
    /** Logger. */
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(Polygon2f.class);

    private List<Line2f> edges;

    /* TODO: use position. */
    private Vector2f position;

    public Polygon2f(final Vector2f position) {
        edges = new ArrayList<Line2f>();
        this.position = position;
    }

    public Polygon2f(final List<Line2f> edges, final Vector2f position) {
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

    public void setEdges(final List<Line2f> edges) {
        this.edges = edges;
    }

    public void setPosition(final Vector2f position) {
        this.position = position;
    }

    public boolean pointInsidePolygon(final Vector2f point) {
        /* Bitfields. */
        int ccwPartition = 0;
        int cwPartition = 0;

        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).isBackFacing(point)) {
                ccwPartition |= 1 << i;
            } else {
                cwPartition |= 1 << i;
            }
        }

        return ccwPartition == 0 || cwPartition == 0;
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
    public float circleCollision(final Circle circle, final Vector2f velocity) {
        float collisionTime = Float.MAX_VALUE;
        boolean collision = false;

        /* Check if it is already colliding. */
        final Vector2f closest = closestPointOnPolygon(circle.getPos());

        if (circle.containsPoint(closest)
                || pointInsidePolygon(circle.getPos())) {
            return 0;
        }

        for (final Line2f edge : edges) {
            final float time = edge.circleLineCollision(circle, velocity);

            if (time >= 0 && time < collisionTime) {
                collision = true;
                collisionTime = time;
            }
        }

        return collision ? collisionTime : -1.0f;
    }

    public Vector2f closestPointOnPolygon(final Vector2f point) {
        Vector2f closest = new Vector2f();
        float distance = Float.MAX_VALUE;

        for (final Line2f edge : edges) {
            final Vector2f closestToEdge = edge.projectionPointToLine(point);
            final float distSquared = point.sub(closestToEdge).lengthSquared();
            if (distSquared < distance) {
                distance = distSquared;
                closest = closestToEdge;
            }
        }

        return closest;
    }

    public GeometricalCollisionData circleCollisionData(final Circle circle,
            final Vector2f velocity) {
        final float time = circleCollision(circle, velocity);

        if (time < 0) {
            return new GeometricalCollisionData(false, 0, null, null);
        }

        final Vector2f circlePos = circle.getPos().add(velocity.scale(time));
        final Vector2f polygonPoint = closestPointOnPolygon(circlePos);
        Vector2f circlePoint = new Circle(circlePos, circle.getRadius())
                .closestPointOnCircle(polygonPoint);

        Vector2f normal = circlePos.sub(polygonPoint).normalize();
        Vector2f penetration = polygonPoint.sub(circlePoint);

        if (pointInsidePolygon(circlePos)) {
            /* We are deep in the polygon! */
            if (!new Circle(circlePos, circle.getRadius())
                    .isPointInCircle(polygonPoint)) {
                normal = normal.scale(-1.0f);
            }

            circlePoint = circlePos.add(normal.scale(circle.getRadius()));
            penetration = polygonPoint.sub(circlePoint);
        }

        return new GeometricalCollisionData(true, time, normal, penetration);
    }
}
