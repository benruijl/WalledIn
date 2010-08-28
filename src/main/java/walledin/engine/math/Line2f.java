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


/**
 * This class describes a 2-dimensional line or line segment.
 * 
 * @author Ben Ruijl
 * 
 */
public class Line2f {
    /** Normalized direction. */
    private final Vector2f dir;

    private final boolean finite;
    private final Vector2f begin;
    private Vector2f end;

    public Line2f(final Vector2f begin, final Vector2f end, final boolean finite) {
        this.finite = finite;
        this.begin = begin;
        this.end = end;
        dir = end.sub(begin).normalize();
    }

    public Line2f(final Vector2f pointOnLine, final Vector2f dir) {
        finite = false;
        begin = pointOnLine;
        this.dir = dir.normalize();
    }

    public boolean isFinite() {
        return finite;
    }

    public Vector2f getDir() {
        return dir;
    }
    
    public boolean isBackFacing(final Vector2f point) {
        return point.sub(begin).cross(dir) <= 0;
    }

    /**
     * Returns the projection of a point to a line or line segment.
     * 
     * @param point
     *            Point
     * @return Closest point on line
     */
    public Vector2f projectionPointToLine(final Vector2f point) {
        /* Project point to line. */
        final Vector2f dirVec = point.sub(begin);
        final float projLength = dirVec.dot(dir);

        if (finite && projLength <= 0) {
            return begin;
        } else {
            if (finite && projLength >= end.sub(begin).length()) {
                return end;
            }
        }

        return dir.scale(projLength).add(begin);
    }

    /**
     * Returns the distance of a point to the line or the line segment.
     * 
     * @param point
     *            Point
     * @return Distance
     */
    public float distancePointToLine(final Vector2f point) {
        return point.sub(projectionPointToLine(point)).length();
    }

    /**
     * Calculates the time of intersection between a moving circle and a line or
     * a line segment. Treats the circle-line system as a point-cylinder
     * problem.
     * 
     * @param circle
     *            Circle
     * @param velocity
     *            Velocity of circle
     * @return Time. If it is less than zero, no collision happened.
     */
    public float circleLineCollision(final Circle circle,
            final Vector2f velocity) {
        /*
         * We know that (a x b)^2 = r^2, where a is the distance from the center
         * of the sphere to the beginning, b the normalized direction of the
         * line and r the radius of the circle.
         */
        final Vector2f l = end.sub(begin);
        final float a = velocity.cross(l) * velocity.cross(l);
        final float b = 2.0f * velocity.cross(l)
                * circle.getPos().sub(begin).cross(l);
        final float c = circle.getPos().sub(begin).cross(l)
                * circle.getPos().sub(begin).cross(l) - circle.getRadius()
                * circle.getRadius() * l.lengthSquared();
        float d = b * b - 4.0f * a * c;

        if (d < 0) {
            /* There is no collision. */
            return -1;
        }

        d = (float) Math.sqrt(d);
        float t0 = (-b - d) / (2.0f * a);
        float t1 = (-b + d) / (2.0f * a);

        if (t0 > t1) {
            final float temp = t0;
            t0 = t1;
            t1 = temp;
        }

        /* The point misses the infinite cylinder. */
        if (t0 > 1.0f || t1 < 0.0f) {
            return -1;
        }

        /*
         * Check if the point hit the sides, or if it hit the middle part. If it
         * hit the edges the check should be extended to a point on circle
         * check, because the region we are looking at is a rounded off
         * rectangle.
         */
        float tEdge = t0;
        if (tEdge < 0.0f) {
            tEdge = 0.0f;
        }
        final Vector2f edge = circle.getPos().add(velocity.scale(tEdge));

        /* Project edge to line. */
        final float e = edge.sub(begin).dot(dir);

        if (e < 0.0f) {
            return circle.pointCollision(begin, velocity);
        } else if (e > 1.0f) {
            return circle.pointCollision(end, velocity);
        } else {
            return t0;
        }
    }
}
