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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.MessageType;
import walledin.game.network.client.Client;

public class ScreenManager {
    /** Logger */
    private static final Logger LOG = Logger.getLogger(ScreenManager.class);

    /** Screen types. */
    public enum ScreenType {
        MAIN_MENU, GAME, SERVER_LIST
    }

    /** Map of typed screens. */
    private final Map<ScreenType, Screen> typedScreens;
    /** List of screens. */
    private final List<Screen> screens;
    /** Entity list of all screens together. */
    private final EntityManager entityManager;
    /** Client entity factory. */
    private final EntityFactory entityFactory;
    /** Map of shared fonts. */
    private final Map<String, Font> fonts;
    /** Shared cursor. */
    private Entity cursor;
    /** Shared renderer. */
    private final Renderer renderer;
    /** Player name. */
    private String playerName;
    /** Client using this screen manager. */
    private final Client client;
    /** Keeps track is the cursor has to be drawn. */
    private boolean drawCursor;
    /** Focused screen. Only one screen can be focused. */
    private Screen focusedScreen;

    /**
     * Creates a screen manager.
     * 
     * @param renderer
     *            Renderer used by screen manager
     */
    public ScreenManager(final Client client, final Renderer renderer) {
        typedScreens = new ConcurrentHashMap<ScreenType, Screen>();
        screens = new ArrayList<Screen>();
        fonts = new HashMap<String, Font>();
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);

        this.client = client;
        this.renderer = renderer;
        drawCursor = true;
    }

    public final EntityManager getEntityManager() {
        return entityManager;
    }

    public final EntityFactory getEntityFactory() {
        return entityFactory;
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
     * Registers the name of the player. Useful when the player object is
     * needed.
     * 
     * @param name
     *            Name of player
     */
    public final void setPlayerName(final String name) {
        playerName = name;
    }

    public final String getPlayerName() {
        return playerName;
    }

    /**
     * Adds font to shared list.
     * 
     * @param name
     *            Name of font
     * @param font
     *            Font object
     */
    public final void addFont(final String name, final Font font) {
        fonts.put(name, font);
    }

    /**
     * Gets one of the shared fonts.
     * 
     * @param name
     *            Name of the font
     * @return Font object if in list, else null.
     */
    public final Font getFont(final String name) {
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
    public final void addScreen(final ScreenType type, final Screen screen) {
        typedScreens.put(type, screen);
        screens.add(screen);
        screen.registerScreenManager(this);
    }

    /**
     * Adds screen to the list.
     * 
     * @param screen
     *            Screen to add
     */
    public final void addScreen(final Screen screen) {
        screens.add(screen);
        screen.registerScreenManager(this);
    }

    public final void removeScreen(final Screen screen) {
        if (!screens.remove(screen)) {
            LOG.warn("Tried to remove screen that is not in the list");
        }

        if (focusedScreen == screen) {
            focusedScreen = null;
        }

        /* If it is a typed screen, remove the map */
        typedScreens.remove(screen);
    }

    /**
     * Fetch a screen from the list.
     * 
     * @param type
     *            Type of requested screen
     * @return Screen if exists, else null
     */
    public final Screen getScreen(final ScreenType type) {
        return typedScreens.get(type);
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
        /* Update all entities */
        getEntityManager().update(delta);

        /* Update cursor position */
        if (cursor != null) {
            cursor.setAttribute(Attribute.POSITION,
                    renderer.screenToWorld(Input.getInstance().getMousePos()));
        }

        Set<Integer> keysDown = Input.getInstance().getKeysDown();

        for (int i = 0; i < screens.size(); i++) {
            if (screens.get(i).isVisible()) {
                screens.get(i).update(delta);

                if (getFocusedScreen() == null && keysDown.size() > 0) {
                    screens.get(i).sendKeyDownMessage(
                            new ScreenKeyEvent(keysDown));
                }
            }
        }

        if (getFocusedScreen() != null) {
            /* If there is a focused window, send the keys to that window. */
            if (keysDown.size() > 0) {
                getFocusedScreen().sendKeyDownMessage(
                        new ScreenKeyEvent(Input.getInstance().getKeysDown()));
            }
        }

        /*
         * Do the check again, because the key event response could change the
         * focused screen.
         */
        if (getFocusedScreen() != null) {
            Screen screen = getFocusedScreen()
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
        for (int i = 0; i < screens.size(); i++) {
            if (screens.get(i).isVisible()) {
                renderer.pushMatrix();
                renderer.translate(screens.get(i).getPosition());
                screens.get(i).draw(renderer);
                renderer.popMatrix();
            }
        }

        if (cursor != null && drawCursor) {
            getCursor().sendMessage(MessageType.RENDER, renderer);
        }

        /* Show FPS for debugging */
        renderer.startHUDRendering();
        final Font font = getFont("arial20");
        font.renderText(renderer, "FPS: " + renderer.getFPS(), new Vector2f(
                630, 20));
        renderer.stopHUDRendering();
    }

    public final void setDrawCursor(final boolean drawCursor) {
        this.drawCursor = drawCursor;
    }

    public final boolean isDrawCursor() {
        return drawCursor;
    }

    public final Entity getCursor() {
        return cursor;
    }

    public final void setCursor(final Entity cursor) {
        this.cursor = cursor;
    }

    /**
     * The given screen will have the focus. This means that it is the only
     * screen receiving input.
     * 
     * @param screen
     *            Screen. Can be null.
     */
    public void setFocusedScreen(final Screen screen) {
        focusedScreen = screen;
    }

    /**
     * Returns the screen that currently has the focus or null if no screen has
     * the focus.
     * 
     * @return Focused screen
     */
    public Screen getFocusedScreen() {
        return focusedScreen;
    }

    /**
     * Kill the application. Sends the dispose message to the client.
     */
    public final void dispose() {
        client.dispose();
    }

    /**
     * Get the client.
     * 
     * @return Client that owns this screen manager.
     */
    public final Client getClient() {
        return client;
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
}
