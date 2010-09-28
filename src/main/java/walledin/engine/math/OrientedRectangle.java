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

public class OrientedRectangle {
    /** The four points of the rectangle. */
    private final Vector2f a, b, c, d;

    public OrientedRectangle(Vector2f a, Vector2f b, Vector2f c, Vector2f d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * Creates an oriented rectangle with a rotation around the center.
     * @param rect Rectangle
     * @param angle Rotation
     */
    public OrientedRectangle(Rectangle rect, float angle) {
        final Matrix2f rot = new Matrix2f(angle);
        final Vector2f center = rect.getCenter();
        
        a = rot.apply(rect.getLeftTop().sub(center)).add(center);
        b = rot.apply(rect.getLeftBottom().sub(center)).add(center);
        c = rot.apply(rect.getRightBottom().sub(center)).add(center);
        d = rot.apply(rect.getRightTop().sub(center)).add(center);
    }

    public OrientedRectangle translate(Vector2f offset) {
        return new OrientedRectangle(a.add(offset), b.add(offset),
                c.add(offset), d.add(offset));
    }

    public Polygon2f asPolygon() {
        final List<Line2f> edges = new ArrayList<Line2f>();
        edges.add(new Line2f(a, b, true));
        edges.add(new Line2f(b, c, true));
        edges.add(new Line2f(c, d, true));
        edges.add(new Line2f(d, a, true));

        return new Polygon2f(edges, new Vector2f());
    }

}
