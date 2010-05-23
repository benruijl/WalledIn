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
package walledin.math;

/**
 * A class for 2d matrices.
 * 
 * @author ben
 */
public class Matrix2f {
	private final float[] m = new float[4];

	public Matrix2f() {
	}

	public Matrix2f(final float a, final float b, final float c, final float d) {
		m[0] = a;
		m[1] = b;
		m[2] = c;
		m[3] = d;
	}

	/**
	 * Create a rotation matrix
	 * 
	 * @param rot
	 *            Rotation in <b>radians</b>
	 */
	public Matrix2f(final double rot) {
		m[0] = m[4] = (float) Math.cos(rot);
		m[1] = (float) Math.sin(rot);
		m[2] = -m[1];
	}

	public Vector2f apply(final Vector2f vec) {
		return new Vector2f(vec.getX() * m[0] + vec.getY() * m[2], vec.getX()
				* m[1] + vec.getY() * m[3]);
	}

	public Matrix2f transpose() {
		return new Matrix2f(m[0], m[2], m[1], m[3]);
	}

	public float determinant() {
		return m[0] * m[3] - m[1] * m[2];
	}

}
