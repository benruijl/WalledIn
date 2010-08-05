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

/**
 * 
 * @author ben
 */
public class Vector2i {
    private final int x;
    private final int y;

    public Vector2i() {
        x = 0;
        y = 0;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }

    public Vector2i(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(final Vector2i vec) {
        return new Vector2i(x + vec.x, y + vec.y);
    }

    public int dot(final Vector2i vec) {
        return x * vec.x + y * vec.y;
    }

    public int lengthSquared() {
        return dot(this);
    }

    public Vector2f asVector2f() {
        return new Vector2f(x, y);
    }

}
