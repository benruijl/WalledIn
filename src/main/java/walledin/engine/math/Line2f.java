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

import com.sun.org.apache.bcel.internal.generic.SWAP;

/**
 * This class describes a 2-dimensional line or line segment.
 * 
 * @author Ben Ruijl
 * 
 */
public class Line2f {
    /** Normalized direction. */
    private Vector2f dir;

    private boolean finite;
    private Vector2f begin;
    private Vector2f end;

    public Line2f(Vector2f begin, Vector2f end, boolean finite) {
        this.finite = finite;
        this.begin = begin;
        this.end = end;
        dir = end.sub(begin).normalize();
    }

    public Line2f(Vector2f pointOnLine, Vector2f dir) {
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

    /**
     * Returns the projection of a point to a line or line segment.
     * 
     * @param point
     *            Point
     * @return Closest point on line
     */
    public Vector2f projectionPointToLine(Vector2f point) {
        /* Project point to line. */
        Vector2f dirVec = point.sub(begin);
        float projLength = dirVec.dot(dir);

        if (finite && projLength < 0) {
            return begin;
        } else {
            if (finite && projLength > end.sub(begin).length()) {
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
    public float distancePointToLine(Vector2f point) {
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
    public float circleLineCollision(Circle circle, Vector2f velocity) {
        /*
         * We know that (a x b)^2 = r^2, where a is the distance from the center
         * of the sphere to the beginning, b the normalized direction of the
         * line and r the radius of the circle.
         */
        float a = velocity.cross(dir) * velocity.cross(dir);
        float b = 2.0f * velocity.cross(dir)
                * circle.getPos().sub(begin).cross(dir);
        float c = circle.getPos().sub(begin).cross(dir)
                * circle.getPos().sub(begin).cross(dir) - circle.getRadius()
                * circle.getRadius();
        float d = b * b - 4 * a * c;

        if (d < 0) {
            /* There is no collision. */
            return -1;
        }

        d = (float) Math.sqrt(d);
        float t0 = (-b - d) / (2.0f * a);
        float t1 = (-b + d) / (2.0f * a);

        if (t0 > t1) {
            float temp = t0;
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
        if (t0 < 0.0f) {
            t0 = 0.0f;
        }

        Vector2f edge = circle.getPos().add(velocity.scale(t0));
        
        /* Project edge to line. */
        float e = edge.sub(begin).dot(dir);

        if (e < 0.0f) {
            return circle.pointCollision(begin, velocity);
        } else if (e > 1.0f) {
            return circle.pointCollision(end, velocity);
        } else {
            return t0;
        }
    }
}
