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
 * 
 * @author ben
 */
public class Vector2f {
	public final float x;
	public final float y;

	public Vector2f() {
		x = 0;
		y = 0;
	}

	public Vector2f(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2f(final Vector2f vector) {
		x = vector.x;
		y = vector.y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public Vector2f add(final Vector2f vec) {
		return new Vector2f(x + vec.x, y + vec.y);
	}

	public Vector2f sub(final Vector2f vec) {
		return new Vector2f(x - vec.x, y - vec.y);
	}

	public float dot(final Vector2f vec) {
		return x * vec.x + y * vec.y;
	}

	public float lengthSquared() {
		return dot(this);
	}

	public Vector2f scale(final float amount) {
		return new Vector2f(x * amount, y * amount);
	}
}
