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

public class Circle extends Geometry {
    private Vector2f pos;
    private float radius;

    public Circle(final Vector2f pos, final float radius) {
        super();
        this.pos = pos;
        this.radius = radius;
    }

    public Circle() {
        pos = new Vector2f();
    }

    public Vector2f getPos() {
        return pos;
    }

    public float getRadius() {
        return radius;
    }

    public void setPos(final Vector2f pos) {
        this.pos = pos;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
    }
    
    /**
     * Returns the 'depth' of the intersection.
     * @param circ Circle
     * @return Depth of collision. Can be negative if there is no collision.
     */
    public float intersectionDepth(final Circle circ) {
        return (getRadius() + circ.getRadius())
        * (getRadius() + circ.getRadius()) - getPos().sub(
        circ.getPos()).lengthSquared();
    }

    @Override
    public Circle translate(final Vector2f pos) {
        return new Circle(this.pos.add(pos), radius);
    }

    @Override
    public boolean intersects(final Geometry geometry) {
        // uses double callback trick
        return geometry.intersects(this);
    }

    @Override
    public boolean intersects(final Circle circ) {
        return (getRadius() + circ.getRadius())
                * (getRadius() + circ.getRadius()) >= getPos().sub(
                circ.getPos()).lengthSquared();
    }

    @Override
    public boolean intersects(final Rectangle rect) {
        return Geometry.intersects(rect, this);
    }

    @Override
    public Circle asCircumscribedCircle() {
        return this;
    }

    @Override
    public Circle asInscribedCircle() {
        return this;
    }

    /**
     * Returns the smallest axis-aligned rectangle that contains the circle.
     * 
     * @return Rectangle
     */
    @Override
    public Rectangle asRectangle() {
        return new Rectangle(pos.getX() - radius, pos.getY() - radius,
                2 * radius, 2 * radius);
    }

    @Override
    public boolean containsPoint(final Vector2f point) {
        return pos.sub(pos).lengthSquared() < radius * radius;
    }
    
    /**
     * Returns the time at which a moving point collides with the circle.
     * @param point Point
     * @param velocity Velocity of point
     * @return time at which collision happened. Is negative when none happened.
     */
    public float pointCollision(final Vector2f point, final Vector2f velocity) {
        Vector2f H = pos.sub(point);
        
        float a = velocity.lengthSquared();
        float b = 2.0f * (velocity.dot(H));
        float c = H.dot(H) - radius * radius;
        float d = (b*b) - (4.0f * a * c);
        
        // point missed by infinite ray
        if (d < 0.0f) {
            return -1.0f;
        }
        
        d = (float) Math.sqrt(d);
        float t0 = (-b - d) / (2.0f * a);
        float t1 = (-b + d) / (2.0f * a);

        // sort times
        if (t0 > t1) {
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }
        
        // point missed by ray range
        if (t0 > 1.0f || t1 < 0.0f) {
            return -1.0f;
        }
        
        return t0;
    }

}
