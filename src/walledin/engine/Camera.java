package walledin.engine;

/**
 * 
 * @author ben
 */
public class Camera {
	private Vector2f pos;
	private Vector2f scale;
	private float rot;

	public Camera() {
		pos = new Vector2f();
		scale = new Vector2f(1.0f, 1.0f);
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(final Vector2f pos) {
		this.pos = pos;
	}

	/**
	 * Get rotation in <b>radians</b>
	 * @return Returns rotation in radians
	 */
	public float getRot() {
		return rot;
	}

	public void setRot(final float rot) {
		this.rot = rot;
	}

	public Vector2f getScale() {
		return scale;
	}

	public void setScale(final Vector2f scale) {
		this.scale = scale;
	}

}
