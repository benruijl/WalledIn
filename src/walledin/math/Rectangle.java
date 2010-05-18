/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.math;

/**
 * 
 * @author ben
 */
public class Rectangle {
	private final float x;
	private final float y;
	private final float width;
	private final float height;

	public Rectangle() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
	}

	public Rectangle(final float x, final float y, final float width,
			final float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public float getLeft() {
		return x;
	}

	public float getTop() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getRight() {
		return x + width;
	}

	public float getBottom() {
		return y + height;
	}

	public Vector2f getLeftTop() {
		return new Vector2f(x, y);
	}

	public Vector2f getRightTop() {
		return new Vector2f(getRight(), y);
	}

	public Vector2f getRightBottom() {
		return new Vector2f(getRight(), getBottom());
	}

	public Vector2f getLeftBottom() {
		return new Vector2f(x, getBottom());
	}

	public boolean intersects(final Rectangle rect) {
		return rect.getRight() > getLeft() && rect.getLeft() < getRight()
				&& rect.getBottom() > getTop() && rect.getTop() < getBottom();
	}

	public Rectangle setPos(final Vector2f vPos) {
		return new Rectangle(vPos.x, vPos.y, width, height);
	}

	public Rectangle translate(final Vector2f vPos) {
		return new Rectangle(x + vPos.x, y + vPos.y, width, height);
	}

	public Rectangle scaleSize(final Vector2f scale) {
		return new Rectangle(x, y, width * scale.getX(), height * scale.getY());
	}

	public Rectangle scaleAll(final Vector2f scale) {
		return new Rectangle(x * scale.getX(), y * scale.getY(), width
				* scale.getX(), height * scale.getY());
	}
}
