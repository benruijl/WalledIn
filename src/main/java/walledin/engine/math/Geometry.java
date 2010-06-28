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

import walledin.util.Utils;

public abstract class Geometry {
    public abstract boolean intersects(Geometry geometry);

    public abstract boolean intersects(Rectangle rect);

    public abstract boolean intersects(Circle circ);

    public static boolean intersects(final Rectangle rect, final Circle circ) {
        final float closestX = Utils.clamp(circ.getPos().getX(),
                rect.getLeft(), rect.getRight());
        final float closestY = Utils.clamp(circ.getPos().getY(), rect.getTop(),
                rect.getBottom());

        final Vector2f dist = circ.getPos().sub(
                new Vector2f(closestX, closestY));

        return dist.lengthSquared() < circ.getRadius() * circ.getRadius();
    }

    public abstract Rectangle asRectangle();

    public abstract Circle asCircumscribedCircle();

    public abstract Circle asInscribedCircle();

    public abstract Geometry translate(final Vector2f pos);
    
    public abstract boolean containsPoint(final Vector2f point);
}
