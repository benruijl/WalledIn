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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.MessageType;
import walledin.game.network.client.Client;
import walledin.game.screens.Screen.ScreenState;

public class ScreenManager {

    /** Screen types. */
    public enum ScreenType {
        MAIN_MENU, GAME, SERVER_LIST, DIALOG
    }

    /** List of screens. */
    private final Map<ScreenType, Screen> screens;
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
    /**
     * Client using this screen manager. Useful for quitting the application.
     */
    private final Client client;
    /** Keeps track is the cursor has to be drawn. */
    private boolean drawCursor;

    /**
     * Creates a screen manager.
     * 
     * @param renderer
     *            Renderer used by screen manager
     */
    public ScreenManager(final Client client, final Renderer renderer) {
        screens = new ConcurrentHashMap<ScreenType, Screen>();
        fonts = new HashMap<String, Font>();
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);

        this.client = client;
        this.renderer = renderer;
        this.drawCursor = true;
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
     * Adds screen to the list.
     * 
     * @param type
     *            Type of screen
     * @param screen
     *            Screen to add
     */
    public final void addScreen(final ScreenType type, final Screen screen) {
        screens.put(type, screen);
        screen.registerScreenManager(this);
    }

    /**
     * Fetch a screen from the list.
     * 
     * @param type
     *            Type of requested screen
     * @return Screen if exists, else null
     */
    public final Screen getScreen(final ScreenType type) {
        return screens.get(type);
    }

    /**
     * Updates every screen, the cursor position and the entity manager.
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

        for (final Screen screen : screens.values()) {
            if (screen.isActive()) {
                screen.update(delta);
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
        for (final Screen screen : screens.values()) {
            if (screen.getState() == ScreenState.Visible) {
                screen.draw(renderer);
            }
        }

        if (cursor != null && drawCursor) {
            getCursor().sendMessage(MessageType.RENDER, renderer);
        }
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
}
