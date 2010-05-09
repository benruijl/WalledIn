package walledin.engine.math;

/**
 * 
 * @author ben
 */
public class Vector2f {
	public float x;
	public float y;

	public Vector2f() {

	}

	public Vector2f(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2f(Vector2f vector) {
		this.x = vector.x;
		this.y = vector.y;
	}

	public float x() {
		return x;
	}

	public float y() {
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

	public void scale(float amount) {
		x *= amount;
		y *= amount;
	}
}
