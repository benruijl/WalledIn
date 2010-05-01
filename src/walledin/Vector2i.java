/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin;

/**
 *
 * @author ben
 */
public class Vector2i {
    public int x, y;

    public Vector2i()
    {

    }

    public Vector2i(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(Vector2i vec)
    {
        return new Vector2i(x + vec.x, y + vec.y);
    }

    public int dot(Vector2i vec)
    {
        return x * vec.x + y * vec.y;
    }

    public int lengthSquared()
    {
        return dot(this);
    }

}
