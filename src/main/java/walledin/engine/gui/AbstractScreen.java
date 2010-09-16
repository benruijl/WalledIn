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
package walledin.engine.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.input.Input;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

/**
 * A game screen. Can be anything from a background to a pop-up dialog.
 * 
 * @author Ben Ruijl
 * 
 */
public abstract class AbstractScreen {
    /** Screen states. */
    public enum ScreenState {
        /** The screen is visible. */
        Visible,
        /** The screen is hidden. */
        Hidden
    }

    /** Parent of this screen. */
    private final AbstractScreen parent;

    /** Child screens of this screen sorted by z-index. */
    private final SortedSet<AbstractScreen> children;

    /** Manager of this screen. */
    private ScreenManager manager;

    /** State of this screen. */
    private ScreenState state = ScreenState.Hidden;

    /** Z-index. */
    private final int zIndex;

    /** Position. */
    private Vector2f position;

    /** Absolute position. */
    private Vector2f absolutePosition;

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
     * @param z
     *            z-index
     */
    public AbstractScreen(final AbstractScreen parent,
            final Rectangle boudingRect, final int z) {

        children = new TreeSet<AbstractScreen>(
                new Comparator<AbstractScreen>() {

                    @Override
                    public int compare(final AbstractScreen o1,
                            final AbstractScreen o2) {
                        final int z1 = o1.getZIndex();
                        final int z2 = o2.getZIndex();

                        if (z1 == z2) {
                            if (o1.hashCode() == o2.hashCode()) {
                                return 0;
                            }

                            return o1.hashCode() < o2.hashCode() ? -1 : 1;
                        }

                        return z1 - z2;
                    }
                });
        position = new Vector2f();
        mouseListeners = new ArrayList<ScreenMouseEventListener>();
        keyListeners = new ArrayList<ScreenKeyEventListener>();
        this.parent = parent;
        manager = parent.getManager();
        rectangle = boudingRect;
        absolutePosition = parent.getAbsolutePosition();
        zIndex = z;
    }

    /**
     * Creates a new screen.
     * 
     * @param manager
     *            Screen manager.
     * @param boudingRect
     *            Bounding rectangle of this screen. null is allowed for root
     *            screens only.
     * @param z
     *            z-index
     */
    public AbstractScreen(final ScreenManager manager,
            final Rectangle boudingRect, final int z) {

        children = new TreeSet<AbstractScreen>(
                new Comparator<AbstractScreen>() {

                    @Override
                    public int compare(final AbstractScreen o1,
                            final AbstractScreen o2) {
                        final int z1 = o1.getZIndex();
                        final int z2 = o2.getZIndex();

                        if (z1 == z2) {
                            if (o1.hashCode() == o2.hashCode()) {
                                return 0;
                            }

                            return o1.hashCode() < o2.hashCode() ? -1 : 1;
                        }

                        return z1 - z2;
                    }
                });
        position = new Vector2f();
        mouseListeners = new ArrayList<ScreenMouseEventListener>();
        keyListeners = new ArrayList<ScreenKeyEventListener>();
        parent = null;
        this.manager = manager;
        rectangle = boudingRect;
        absolutePosition = new Vector2f();
        zIndex = z;
    }

    /**
     * To be called after screen is added to the list. Do not call on
     * beforehand, because some functions may require a parent screen manager.
     */
    public void initialize() {
    }

    /**
     * Finds the smallest screen containing a certain position.
     * 
     * @param pos
     *            Position to check
     * 
     * @return Returns a Screen on success and null on failure.
     */
    public final AbstractScreen getSmallestScreenContainingPoint(
            final Vector2f pos) {
        if (pointInScreen(pos)) {
            for (final AbstractScreen screen : children) {
                if (screen.isVisible()) {
                    final AbstractScreen b = screen
                            .getSmallestScreenContainingPoint(pos);

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
     * Finds the smallest screen containing the mouse cursor.
     * 
     * @return Returns a Screen on success and null on failure.
     */
    public final AbstractScreen getSmallestScreenContainingCursor() {
        return getSmallestScreenContainingPoint(Input.getInstance()
                .getMousePos().asVector2f());
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
            for (final AbstractScreen screen : children) {
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
    public final void dispose() {
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

        for (final AbstractScreen screen : children) {
            if (screen.getState() == ScreenState.Visible) {
                screen.update(delta);
            }
        }
    }

    /**
     * Checks if the screen is visible locally. It does not check if its parent
     * is visible.
     * 
     * @return True if visible, else false
     */
    public final boolean isVisible() {
        return state == ScreenState.Visible;
    }

    /**
     * Draws the screen and its children.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public void draw(final Renderer renderer) {
        for (final AbstractScreen screen : children) {
            if (screen.getState() == ScreenState.Visible) {
                renderer.pushMatrix();
                renderer.translate(screen.getPosition());
                screen.draw(renderer);
                renderer.popMatrix();
            }
        }
    }

    /**
     * Sets the focus to this screen.
     */
    public final void setFocus() {
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

    /**
     * Returns the current state.
     * 
     * @return Current state
     */
    public final ScreenState getState() {
        return state;
    }

    /**
     * Returns the z-index.
     * 
     * @return z-index
     */
    public final int getZIndex() {
        return zIndex;
    }

    /**
     * Shows the window and makes it active.
     */
    public final void show() {
        state = ScreenState.Visible;
        onVisibilityChanged(true);
    }

    /**
     * Hides the window and makes it inactive.
     */
    public final void hide() {
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

    /**
     * Gets the screen manager. The screen manager is inherited from the parent
     * or is set directly.
     * 
     * @return Screen manager
     */
    public final ScreenManager getManager() {
        return manager;
    }

    /**
     * Adds a child to the list. This child will be updated and drawn
     * automatically, if visible.
     * 
     * @param childScreen
     *            Child screen
     */
    public final boolean addChild(final AbstractScreen childScreen) {
        return children.add(childScreen);
    }

    /**
     * Removes a child from this parent class.
     * 
     * @param childScreen
     *            Child screen
     */
    public final boolean removeChild(final AbstractScreen childScreen) {
        return children.remove(childScreen);
    }

    /**
     * Returns the bounding rectangle.
     * 
     * @return Bounding rectangle
     */
    public final Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Returns the position in relative coordinates.
     * 
     * @return Position
     */
    public final Vector2f getPosition() {
        return position;
    }

    /**
     * Returns the position in relative coordinates.
     * 
     * @return Position
     */
    public final Vector2f getAbsolutePosition() {
        return absolutePosition;
    }

    /**
     * Updates the absolute position for this window and all its children. This
     * is called by the parent when it is moved.
     */
    public final void updateAbsolutePosition() {
        if (parent != null) {
            absolutePosition = parent.getAbsolutePosition().add(position);
        } else {
            absolutePosition = position;
        }

        // update all children
        for (final AbstractScreen child : children) {
            child.updateAbsolutePosition();
        }
    }

    /**
     * Updates the position and the absolute position.
     * 
     * @param position
     *            The new relative position
     */
    public final void setPosition(final Vector2f position) {
        this.position = position;

        updateAbsolutePosition();
    }

    /**
     * Gets the parent of this screen.
     * 
     * @return Parent screen or null
     */
    public final AbstractScreen getParent() {
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
    public final boolean pointInScreen(final Vector2f point) {
        if (rectangle == null) {
            return true;
        }

        return rectangle.translate(absolutePosition).containsPoint(point);
    }

    /**
     * Adds a mouse event listener to this screen.
     * 
     * @param listener
     *            Mouse event listener
     */
    public final void addMouseEventListener(
            final ScreenMouseEventListener listener) {
        mouseListeners.add(listener);
    }

    /**
     * Sends the mouse hover message when the mouse hovers over this screen.
     * 
     * @param e
     *            Event message
     */
    public final void sendMouseHoverMessage(final ScreenMouseEvent e) {
        for (final ScreenMouseEventListener listener : mouseListeners) {
            listener.onMouseHover(e);
        }
    }

    /**
     * Send the mouse button down message when a button is down and the mouse
     * hovers over this screen.
     * 
     * @param e
     *            Event message
     */
    public final void sendMouseDownMessage(final ScreenMouseEvent e) {
        for (final ScreenMouseEventListener listener : mouseListeners) {
            listener.onMouseDown(e);
        }
    }

    /**
     * Send the mouse clicked message when a button is clicked and the mouse
     * hovers over this screen.
     * 
     * @param e
     *            Event message
     */
    public final void sendMouseClickedMessage(final ScreenMouseEvent e) {
        for (final ScreenMouseEventListener listener : mouseListeners) {
            listener.onMouseClicked(e);
        }
    }

    /**
     * Adds a key event listener to this screen.
     * 
     * @param listener
     *            Key event listener
     */
    public final void addKeyEventListener(final ScreenKeyEventListener listener) {
        keyListeners.add(listener);
    }

    /**
     * Sends the key down message when a key is pressed and this screen has the
     * focus. The message will be sent to the children as well.
     * 
     * @param e
     *            Event message
     */
    public final void sendKeyDownMessage(final ScreenKeyEvent e) {
        for (final ScreenKeyEventListener listener : keyListeners) {
            listener.onKeyDown(e);
        }

        for (final AbstractScreen screen : children) {
            if (screen.isVisible()) {
                screen.sendKeyDownMessage(e);
            }
        }
    }

    /**
     * Checks if this screen has the focus.
     * 
     * @return True if it has the focus, else false.
     */
    private boolean hasFocus() {
        return getManager().getFocusedScreen() == this;
    }

    /**
     * Returns the current active font.
     * 
     * @return Active font or null if none is set.
     */
    public final Font getFont() {
        return font;
    }

    /**
     * Sets the active font.
     * 
     * @param font
     *            Font
     */
    public final void setFont(final Font font) {
        this.font = font;
    }
}
