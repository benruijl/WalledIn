/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin;

/**
 *
 * @author ben
 */
public class Rectangle {

    private float x, y, width, height;

    public Rectangle() {
    }

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Float left() {
        return x;
    }

    public Float top() {
        return y;
    }

    public Float width() {
        return width;
    }

    public Float height() {
        return height;
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public Vector2f leftTop() {
        return new Vector2f(x, y);
    }

    public Vector2f rightTop() {
        return new Vector2f(right(), y);
    }

    public Vector2f rightBottom() {
        return new Vector2f(right(), bottom());
    }

    public Vector2f leftBottom() {
        return new Vector2f(x, bottom());
    }

    public boolean intersects(Rectangle rect) {
        return rect.right() > left() && rect.left() < right()
                && rect.bottom() > top() && rect.top() < bottom();
    }

    public Rectangle addOffset(Vector2f vPos)
    {
        return new Rectangle(x + vPos.x, y + vPos.y, width, height);
    }
}
