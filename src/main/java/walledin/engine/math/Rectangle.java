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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Rectangle class.
 * 
 * @author Ben Ruijl
 */
public class Rectangle extends AbstractGeometry {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public Rectangle() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    public Rectangle(final float x, final float y, final float width,
            final float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getLeft() {
        return x;
    }

    public float getTop() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getRight() {
        return x + width;
    }

    public float getBottom() {
        return y + height;
    }

    public Vector2f getLeftTop() {
        return new Vector2f(x, y);
    }

    public Vector2f getRightTop() {
        return new Vector2f(getRight(), y);
    }

    public Vector2f getRightBottom() {
        return new Vector2f(getRight(), getBottom());
    }

    public Vector2f getLeftBottom() {
        return new Vector2f(x, getBottom());
    }

    public Vector2f getCenter() {
        return new Vector2f(x + getWidth() / 2.0f, y + getHeight() / 2.0f);
    }

    public Rectangle setPos(final Vector2f vPos) {
        return new Rectangle(vPos.getX(), vPos.getY(), width, height);
    }

    @Override
    public Rectangle translate(final Vector2f vPos) {
        return new Rectangle(x + vPos.getX(), y + vPos.getY(), width, height);
    }

    public Rectangle scaleSize(final Vector2f scale) {
        return new Rectangle(x, y, width * scale.getX(), height * scale.getY());
    }

    public Rectangle scaleAll(final Vector2f scale) {
        return new Rectangle(x * scale.getX(), y * scale.getY(), width
                * scale.getX(), height * scale.getY());
    }

    @Override
    public boolean intersects(final AbstractGeometry geometry) {
        // uses double callback trick
        return geometry.intersects(this);
    }

    @Override
    public boolean intersects(final Rectangle rect) {
        return rect.getRight() > getLeft() && rect.getLeft() < getRight()
                && rect.getBottom() > getTop() && rect.getTop() < getBottom();
    }

    @Override
    public boolean intersects(final Circle circ) {
        return AbstractGeometry.intersects(this, circ);
    }

    @Override
    public Circle asCircumscribedCircle() {
        final Vector2f dir = getRightBottom().sub(getLeftTop()).scale(0.5f);
        final Vector2f center = dir.add(getLeftTop());
        final float radius = (float) Math.sqrt(dir.lengthSquared());
        return new Circle(center, radius);
    }

    @Override
    public Circle asInscribedCircle() {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Rectangle asRectangle() {
        return this;
    }

    @Override
    public boolean containsPoint(final Vector2f point) {
        return getRight() > point.getX() && getLeft() < point.getX()
                && getBottom() > point.getY() && getTop() < point.getY();
    }

    public Polygon2f asPolygon() {
        final List<Line2f> edges = new ArrayList<Line2f>();
        edges.add(new Line2f(getLeftTop(), getLeftBottom(), true));
        edges.add(new Line2f(getLeftBottom(), getRightBottom(), true));
        edges.add(new Line2f(getRightBottom(), getRightTop(), true));
        edges.add(new Line2f(getRightTop(), getLeftTop(), true));

        return new Polygon2f(edges, new Vector2f());
    }

    public boolean containsFully(final Rectangle rect) {
        return rect.getLeft() >= getLeft() && rect.getRight() <= getRight()
                && rect.getBottom() <= getBottom() && rect.getTop() >= getTop();
    }
}
