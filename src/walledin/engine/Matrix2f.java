/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin.engine;

/**
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

	public Vector2f Apply(final Vector2f vec) {
		return new Vector2f(vec.x() * m[0] + vec.y() * m[2], vec.x() * m[1]
				+ vec.y() * m[3]);
	}

	public Matrix2f Transpose() {
		return new Matrix2f(m[0], m[2], m[1], m[3]);
	}

	public float Determinant() {
		return m[0] * m[3] - m[1] * m[2];
	}

}
