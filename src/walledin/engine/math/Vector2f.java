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
