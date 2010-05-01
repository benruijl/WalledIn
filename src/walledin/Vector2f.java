/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin;

/**
 *
 * @author ben
 */
public class Vector2f {
    public float x, y;

    public Vector2f()
    {

    }

    public Vector2f(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public float x()
    {
        return x;
    }

    public float y()
    {
        return y;
    }

    public Vector2f add(Vector2f vec)
    {
        return new Vector2f(x + vec.x, y + vec.y);
    }

    public float dot(Vector2f vec)
    {
        return x * vec.x + y * vec.y;
    }

    public float lengthSquared()
    {
        return dot(this);
    }
}
