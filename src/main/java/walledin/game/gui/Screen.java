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
package walledin.game.gui;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

/**
 * A game screen. Can be anything from a background to a pop-up dialog.
 * 
 * @author Ben Ruijl
 * 
 */
public abstract class Screen {
    /** Screen states. */
    public enum ScreenState {
        Visible, Hidden
    }

    /** Parent of this screen. */
    private final Screen parent;

    /** Child screens of this screen. */
    private final List<Screen> children;

    /** Manager of this screen. */
    private ScreenManager manager;

    /** State of this screen. */
    private ScreenState state = ScreenState.Hidden;

    /** Position. */
    private Vector2f position;

    /** Bounding rectangle. */
    private final Rectangle rectangle;

    /** Font. */
    private Font font;

    /** List of mouse event listeners. */
    private final List<ScreenMouseEventListener> mouseListeners;

    /** List of key event listeners. */
    private final List<ScreenKeyEventListener> keyListeners;

    /**
     * Creates a new screen.
     * 
     * @param parent
     *            Parent of the screen. If there is no parent, use the other
     *            constructor.
     * @param boudingRect
     *            Bounding rectangle of this screen. null is allowed for root
     *            screens only.
     */
    public Screen(final Screen parent, final Rectangle boudingRect) {
        children = new ArrayList<Screen>();
        position = new Vector2f();
        mouseListeners = new ArrayList<ScreenMouseEventListener>();
        keyListeners = new ArrayList<ScreenKeyEventListener>();
        this.parent = parent;
        manager = parent.getManager();
        rectangle = boudingRect;
    }

    /**
     * Creates a new screen.
     * 
     * @param manager
     *            Screen manager.
     * @param boudingRect
     *            Bounding rectangle of this screen. null is allowed for root
     *            screens only.
     */
    public Screen(final ScreenManager manager, final Rectangle boudingRect) {
        children = new ArrayList<Screen>();
        position = new Vector2f();
        mouseListeners = new ArrayList<ScreenMouseEventListener>();
        keyListeners = new ArrayList<ScreenKeyEventListener>();
        parent = null;
        this.manager = manager;
        rectangle = boudingRect;
    }

    /**
     * To be called after screen is added to the list. Do not call on
     * beforehand, because some functions may require a parent screen manager.
     */
    public abstract void initialize();

    /**
     * Finds the smallest screen containing the mouse cursor.
     * 
     * @return Returns a Screen on success and null on failure.
     */
    public Screen getSmallestScreenContainingCursor() {
        if (pointInScreen(Input.getInstance().getMousePos().asVector2f())) {
            for (final Screen screen : children) {
                if (screen.isVisible()) {
                    final Screen b = screen.getSmallestScreenContainingCursor();

                    if (b != null) {
                        return b;
                    }
                }
            }

            return this;
        }

        return null;
    }

    /**
     * Checks if the current screen is the smallest one that contains the
     * cursor. It checks if the current window contains the cursor and if so, if
     * none of its children do.
     * 
     * @return True if smallest, else false.
     */
    private boolean isSmallestScreenContainingCursor() {
        if (pointInScreen(Input.getInstance().getMousePos().asVector2f())) {
            for (final Screen screen : children) {
                if (screen.isVisible()) {
                    if (screen.isSmallestScreenContainingCursor()) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Disposes of a screen.
     */
    public void dispose() {
        if (parent == null) {
            getManager().removeScreen(this);
        } else {
            getParent().removeChild(this);
        }
    }

    /**
     * Updates the screen and its active children.
     * 
     * @param delta
     *            Delta time since last update
     */
    public void update(final double delta) {
        /* If there is no focused screen, send the events */
        if (getManager().getFocusedScreen() == null) {
            if (isSmallestScreenContainingCursor()) {
                /* Send mouse hover event */
                sendMouseHoverMessage(new ScreenMouseEvent(this, Input
                        .getInstance().getMousePos().asVector2f()));

                /* Check if mouse pressed */
                if (Input.getInstance().isButtonDown(1)) {
                    sendMouseDownMessage(new ScreenMouseEvent(this, Input
                            .getInstance().getMousePos().asVector2f()));
                }
            }
            
        }

        for (final Screen screen : children) {
            if (screen.getState() == ScreenState.Visible) {
                screen.update(delta);
            }
        }
    }

    public boolean isVisible() {
        return state == ScreenState.Visible;
    }

    /**
     * Draws the screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public void draw(final Renderer renderer) {
        for (final Screen screen : children) {
            if (screen.getState() == ScreenState.Visible) {
                screen.draw(renderer);
            }
        }
    }

    /**
     * Sets the focus to this screen.
     */
    public void setFocus() {
        getManager().setFocusedScreen(this);
    }

    /**
     * Called when the visibility flag of this screen is changed.
     * 
     * @param visible
     *            current value of the flag
     */
    protected void onVisibilityChanged(final boolean visible) {
    }

    public final ScreenState getState() {
        return state;
    }

    /**
     * Shows the window and makes it active.
     */
    public void show() {
        state = ScreenState.Visible;
        onVisibilityChanged(true);
    }

    /**
     * Hides the window and makes it inactive.
     */
    public void hide() {
        state = ScreenState.Hidden;

        if (getManager().getFocusedScreen() == this) {
            getManager().setFocusedScreen(null);
        }

        onVisibilityChanged(false);
    }

    /**
     * Links the screen manager to this screen. This function is usually called
     * by the screen manager on adding the screen.
     * 
     * @param manager
     *            Screen manager
     */
    public final void registerScreenManager(final ScreenManager manager) {
        this.manager = manager;
    }

    public final ScreenManager getManager() {
        return manager;
    }

    public void addChild(final Screen sc) {
        children.add(sc);
    }

    public void removeChild(final Screen sc) {
        children.remove(sc);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(final Vector2f position) {
        this.position = position;
    }

    public Screen getParent() {
        return parent;
    }

    /**
     * Checks if a point is in this window. This will always return true if this
     * Screen is a root screen.
     * 
     * @param point
     *            Point
     * @return True if in window, else false.
     */
    public boolean pointInScreen(final Vector2f point) {
        if (rectangle == null) {
            return true;
        }

        return rectangle.translate(position).containsPoint(point);
    }

    public void addMouseEventListener(final ScreenMouseEventListener listener) {
        mouseListeners.add(listener);
    }
    
    public void sendMouseHoverMessage(final ScreenMouseEvent e) {
        for (final ScreenMouseEventListener listener : mouseListeners) {
            listener.onMouseHover(e);
        }
    }

    public void sendMouseDownMessage(final ScreenMouseEvent e) {
        for (final ScreenMouseEventListener listener : mouseListeners) {
            listener.onMouseDown(e);
        }
    }
    
    public void addKeyEventListener(final ScreenKeyEventListener listener) {
        keyListeners.add(listener);
    }

    public void sendKeyDownMessage(final ScreenKeyEvent e) {
        for (final ScreenKeyEventListener listener : keyListeners) {
            listener.onKeyDown(e);
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(final Font font) {
        this.font = font;
    }
}
