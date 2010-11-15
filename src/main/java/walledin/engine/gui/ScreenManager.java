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

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.input.Input;
import walledin.engine.input.InputEventListener;
import walledin.engine.input.MouseEvent;
import walledin.engine.math.Vector2i;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class ScreenManager implements InputEventListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ScreenManager.class);

    /** Screen types. */
    // TODO: move to game section.
    public enum ScreenType {
        MAIN_MENU, GAME, SERVER_LIST, SELECT_TEAM
    }

    /** Root screen. All screens are children of this one. */
    private final AbstractScreen root;
    /** Map of typed screens. */
    private final Map<ScreenType, AbstractScreen> typedScreens;
    /** Map of shared fonts. */
    private final Map<FontType, Font> fonts;
    /** Shared cursor. */
    private Entity cursor;
    /** Shared renderer. */
    private final Renderer renderer;
    /** Keeps track is the cursor has to be drawn. */
    private boolean drawCursor;
    /** Focused screen. Only one screen can be focused. */
    private AbstractScreen focusedScreen;
    /** Is the mouse clicked? */
    private boolean mouseClicked;
    /** Click position. */
    private Vector2i clickedPosition;
    /** List of released keys. */
    private final Set<Integer> releasedKeys;

    /**
     * Creates a screen manager.
     * 
     * @param renderer
     *            Renderer used by screen manager
     */
    public ScreenManager(final Renderer renderer) {
        root = new AbstractScreen(this, null, -100) {
        };

        /* Set the focus to root. */
        focusedScreen = root;

        typedScreens = new ConcurrentHashMap<ScreenType, AbstractScreen>();
        releasedKeys = new HashSet<Integer>();

        fonts = new HashMap<FontType, Font>();
        this.renderer = renderer;
        drawCursor = true;

        /* Set up the mouse listener. */
        mouseClicked = false;
        Input.getInstance().addListener(this);
    }

    /**
     * Gets the renderer associated with all screens.
     * 
     * @return Renderer
     */
    public final Renderer getRenderer() {
        return renderer;
    }

    /**
     * Adds font to shared list.
     * 
     * @param name
     *            Name of font
     * @param font
     *            Font object
     */
    public final void addFont(final FontType name, final Font font) {
        fonts.put(name, font);
    }

    /**
     * Gets one of the shared fonts.
     * 
     * @param name
     *            Name of the font
     * @return Font object if in list, else null.
     */
    public final Font getFont(final FontType name) {
        return fonts.get(name);
    }

    /**
     * Adds a screen with a predefined type to the list.
     * 
     * @param type
     *            Type of screen
     * @param screen
     *            Screen to add
     */
    public final void addScreen(final ScreenType type,
            final AbstractScreen screen) {
        typedScreens.put(type, screen);
        root.addChild(screen);
        screen.registerScreenManager(this);
    }

    /**
     * Adds screen to the list.
     * 
     * @param screen
     *            Screen to add
     */
    public final void addScreen(final AbstractScreen screen) {
        root.addChild(screen);
        screen.registerScreenManager(this);
    }

    /**
     * Removes a screen from the list. It only removes screens that are
     * registered to the screen manager. It is safest to always use the screen's
     * dispose() function.
     * 
     * @param screen
     *            Screen to remove
     * 
     * @see AbstractScreen#dispose()
     */
    public final void removeScreen(final AbstractScreen screen) {
        if (!root.removeChild(screen)) {
            LOG.warn("Tried to remove screen that is not in the list");
        }

        if (focusedScreen == screen) {
            focusedScreen = root;
        }

        /* If it is a typed screen, remove the map */
        typedScreens.values().remove(screen);
    }

    /**
     * Fetch a screen from the list.
     * 
     * @param type
     *            Type of requested screen
     * @return Screen if exists, else null
     */
    public final AbstractScreen getScreen(final ScreenType type) {
        return typedScreens.get(type);
    }

    @Override
    public void onMouseClicked(final MouseEvent event) {
        /* Sets the flag. */
        mouseClicked = true;
        clickedPosition = event.getPosition();
    }

    /**
     * Updates every screen, the cursor position and the entity manager. It also
     * send the correct events to the screens. Rule: update first, then send
     * event.
     * 
     * @param delta
     *            Delta time
     */
    public final void update(final double delta) {
        /* Update cursor position */
        if (cursor != null) {
            cursor.setAttribute(Attribute.POSITION, Input.getInstance()
                    .getMousePos().asVector2f());
        }

        final Set<Integer> keysDown = Input.getInstance().getKeysDown();

        root.update(delta);

        if (getFocusedScreen() != null) {
            /* If there is a focused window, send the keys to that window. */
            if (keysDown.size() > 0) {
                getFocusedScreen().sendKeyDownMessage(
                        new ScreenKeyEvent(Input.getInstance().getKeysDown()));
            }

            if (releasedKeys.size() > 0) {
                getFocusedScreen().sendKeyUpMessage(
                        new ScreenKeyEvent(releasedKeys));
                releasedKeys.clear();
            }
        }

        /*
         * Do the check again, because the key event response could change the
         * focused screen.
         */
        if (getFocusedScreen() != null) {
            if (mouseClicked) {
                /* Find click screen. */
                final AbstractScreen screen = getFocusedScreen()
                        .getSmallestScreenContainingPoint(
                                clickedPosition.asVector2f());

                if (screen != null) {
                    screen.sendMouseClickedMessage(new ScreenMouseEvent(screen,
                            clickedPosition.asVector2f()));

                    /* Set the clicked screen as the active one. */
                    // setFocusedScreen(screen);
                }

                mouseClicked = false;
            }
        }

        if (getFocusedScreen() != null) {
            final AbstractScreen screen = getFocusedScreen()
                    .getSmallestScreenContainingCursor();

            if (screen != null) {
                /* Send mouse hover event */
                screen.sendMouseHoverMessage(new ScreenMouseEvent(screen, Input
                        .getInstance().getMousePos().asVector2f()));

                /* Check if mouse pressed */
                if (Input.getInstance().isButtonDown(1)) {
                    screen.sendMouseDownMessage(new ScreenMouseEvent(screen,
                            Input.getInstance().getMousePos().asVector2f()));
                }
            }
        }
    }

    /**
     * Draws every visible screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public final void draw(final Renderer renderer) {
        root.draw(renderer);

        if (cursor != null && drawCursor) {
            getCursor().sendMessage(MessageType.RENDER, renderer);
        }
    }

    /**
     * Sets the flag to show or hide the cursor.
     * 
     * @param drawCursor
     *            True if the cursor should be drawn, else false.
     */
    public final void setDrawCursor(final boolean drawCursor) {
        this.drawCursor = drawCursor;
    }

    /**
     * Checks if the 'draw cursor' flag is set.
     * 
     * @return True if set, else false
     */
    public final boolean isDrawCursor() {
        return drawCursor;
    }

    /**
     * Returns the current cursor.
     * 
     * @return The cursor Entity, or null if there is no cursor.
     */
    public final Entity getCursor() {
        return cursor;
    }

    /**
     * Registers a cursor to this screen manager.
     * 
     * @param cursor
     *            Cursor entity
     */
    public final void setCursor(final Entity cursor) {
        this.cursor = cursor;
    }

    /**
     * The given screen will have the focus. This means that it is the only
     * screen receiving input.
     * 
     * @param screen
     *            Screen. If null, the root screen will receive the focus
     */
    public final void setFocusedScreen(final AbstractScreen screen) {
        if (screen != null) {
            focusedScreen = screen;
        } else {
            focusedScreen = root;
        }
    }

    /**
     * Checks if the root is the focused window.
     * 
     * @return True if root has the focus, else false
     */
    public final boolean isRootFocused() {
        return focusedScreen == root;
    }

    /**
     * Returns the screen that currently has the focus or null if no screen has
     * the focus.
     * 
     * @return Focused screen
     */
    public final AbstractScreen getFocusedScreen() {
        return focusedScreen;
    }

    /**
     * Creates a pop-up dialog.
     * 
     * @param text
     *            Text in the dialog.
     */
    public final void createDialog(final String text) {
        final PopupDialog diag = new PopupDialog(this, text);
        addScreen(diag);
        diag.show();
        diag.setFocus();
    }

    @Override
    public void onKeyRelease(KeyEvent key) {
        releasedKeys.add((int) key.getKeyChar());
    }
}
