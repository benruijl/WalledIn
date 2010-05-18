/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin.math;

/**
 * 
 * @author ben
 */
public class Vector2i {
	public final int x;
	public final int y;

	public Vector2i() {
		x = 0;
		y = 0;
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

}
