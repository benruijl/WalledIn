/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin.game;

import walledin.engine.Rectangle;

/**
 *
 * @author ben
 */
public class Tile {
    private final int tileNumber;
    private Rectangle mRect;

    public Tile(int tileNumber) {
        this.tileNumber = tileNumber;
        updateRect();
    }

    public int getTileNumber() {
        return tileNumber;
    }

    public Rectangle getRect() {
        return mRect;
    }



    /**
     * An update of the texture rectangle is required when
     * the tile number is set
     */
    private void updateRect()
    { // FIXME: hardcoded
        mRect = new Rectangle((tileNumber % 16) * 64.0f / 1024.0f, (tileNumber / 16) * 64.0f / 1024.0f, 64.0f / 1024.0f, 64.0f / 1024.0f);
    }

}
