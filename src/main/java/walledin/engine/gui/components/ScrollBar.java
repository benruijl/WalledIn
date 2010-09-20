/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine.gui.components;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class ScrollBar extends AbstractScreen implements
        ScreenMouseEventListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ScrollBar.class);
    private static final float WIDTH = 20f;
    private static final float BUTTON_HEIGHT = 20f;
    private static final float SCROLL_DELAY = 0.1f;

    private int currentIndex;
    private int numEntries;
    private float currentWaitTime;

    private final Rectangle buttonUpRect;
    private final Rectangle buttonDownRect;
    private final int maxVisibleEntries;

    public ScrollBar(final AbstractScreen parent, final int z,
            final int maxVisibleEntries) {
        super(parent, new Rectangle(0, 0, WIDTH, parent.getRectangle()
                .getBottom()), z);

        setPosition(new Vector2f(parent.getRectangle().getRight() - WIDTH,
                parent.getRectangle().getTop()));

        buttonUpRect = new Rectangle(0, 0, WIDTH, BUTTON_HEIGHT);
        buttonDownRect = new Rectangle(0, getRectangle().getBottom()
                - BUTTON_HEIGHT, WIDTH, BUTTON_HEIGHT);

        this.maxVisibleEntries = maxVisibleEntries;
        currentWaitTime = 0f;
        addMouseEventListener(this);
    }

    public void setNumEntries(final int numEntries) {
        this.numEntries = numEntries;

        if (currentIndex >= numEntries) {
            currentIndex = numEntries - 1;
        }

        if (numEntries > 0 && currentIndex < 0) {
            currentIndex = 0;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void update(final double delta) {
        if (currentWaitTime < SCROLL_DELAY) {
            currentWaitTime += delta;
        }

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        renderer.drawFilledRect(buttonUpRect);
        renderer.drawFilledRect(buttonDownRect);

        renderer.drawRectOutline(getRectangle());

        if (currentIndex >= 0 && numEntries - maxVisibleEntries > 0) {
            renderer.setColorRGB(1, 0, 0);
            renderer.drawFilledRect(new Rectangle(
                    0,
                    buttonUpRect.getBottom()
                            + currentIndex
                            / (float) (numEntries - maxVisibleEntries)
                            * (buttonDownRect.getTop()
                                    - buttonUpRect.getBottom() - (buttonDownRect
                                    .getTop() - buttonUpRect.getBottom())
                                    / (numEntries - maxVisibleEntries)), WIDTH,
                    (buttonDownRect.getTop() - buttonUpRect.getBottom())
                            / (numEntries - maxVisibleEntries)));
            renderer.setColorRGB(1, 1, 1);
        }

        super.draw(renderer);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        if (buttonUpRect.translate(getAbsolutePosition()).containsPoint(
                e.getPos())) {
            if (currentIndex > 0) {
                if (currentWaitTime > SCROLL_DELAY) {
                    currentIndex--;
                    currentWaitTime = 0;
                }
            }
        }

        if (buttonDownRect.translate(getAbsolutePosition()).containsPoint(
                e.getPos())) {
            if (currentIndex >= 0
                    && currentIndex < numEntries - maxVisibleEntries) {
                if (currentWaitTime > SCROLL_DELAY) {
                    currentIndex++;
                    currentWaitTime = 0;
                }
            }
        }
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
    }

}
