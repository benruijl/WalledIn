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
	public int x;
	public int y;

	public Vector2i() {

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
