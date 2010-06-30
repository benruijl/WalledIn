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
package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;

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
    private ScreenState state;

    /** Position. */
    private Vector2f position;

    /** Bounding rectangle. */
    private final Rectangle rectangle;

    /** Active flag. */
    protected boolean active = false;

    /**
     * Creates a new screen.
     * 
     * @param parent
     *            Parent of the screen or null of there is no parent.
     * @param boudingRect
     *            Bounding rectangle of this screen. null is allowed for root
     *            screens only.
     */
    public Screen(final Screen parent, final Rectangle boudingRect) {
        children = new ArrayList<Screen>();
        position = new Vector2f();
        this.parent = parent;
        rectangle = boudingRect;
    }

    /**
     * To be called after screen is added to the list. Do not call on
     * beforehand, because some functions may require a parent screen manager.
     */
    public abstract void initialize();

    /**
     * Updates the screen.
     * 
     * @param delta
     *            Delta time since last update
     */
    public void update(final double delta) {
        for (final Screen screen : children) {
            if (screen.isActive()) {
                screen.update(delta);
            }
        }
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

    public final boolean isActive() {
        return active;
    }

    /**
     * Makes the screen active and visible.
     */
    public final void setActiveAndVisible() {
        active = true;
        state = ScreenState.Visible;
    }

    /**
     * Flag the screen as active/inactive. If activated, it will also make the
     * screen visible.
     * 
     * @param active
     *            Activate or deactivate
     */
    public final void setActive(final boolean active) {
        this.active = active;

        if (active) {
            state = ScreenState.Visible;
        }
    }

    public final ScreenState getState() {
        return state;
    }

    public final void setState(final ScreenState state) {
        this.state = state;
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
        sc.registerScreenManager(getManager());
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
     * Checks if a point is in this window.
     * 
     * @param point
     *            Point
     * @return True if in window, else false.
     */
    public boolean pointInScreen(final Vector2f point) {
        return rectangle.translate(position).containsPoint(point);
    }

}
