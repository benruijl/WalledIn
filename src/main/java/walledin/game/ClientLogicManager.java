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
package walledin.game;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.audio.Audio;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.gui.GameScreen;
import walledin.game.gui.MainMenuScreen;
import walledin.game.gui.Screen;
import walledin.game.gui.ScreenManager;
import walledin.game.gui.SelectTeamScreen;
import walledin.game.gui.ServerListScreen;
import walledin.game.gui.ScreenManager.ScreenType;
import walledin.game.network.client.Client;
import walledin.util.SettingsManager;
import walledin.util.Utils;

/**
 * The logic manager for the client.
 * 
 * @author Ben Ruijl
 * 
 */
public final class ClientLogicManager implements RenderListener {
    /** Logger. */
    private static final Logger LOG = Logger
            .getLogger(ClientLogicManager.class);

    /** Flag to check if the assets are loaded. */
    private boolean assetsLoaded = false;
    /** The tile size in the texture. */
    private static final int TILE_SIZE = 64;
    /** Number of tiles per row. */
    private static final int TILES_PER_LINE = 16;
    /** Entity list of all screens together. */
    private final EntityManager entityManager;
    /** Client entity factory. */
    private final EntityFactory entityFactory;
    /** Screen manager. */
    private final ScreenManager screenManager;
    /** Renderer. */
    private final Renderer renderer;
    /** Network client. */
    private final Client client;
    /** The screen of the game (not the menus). */
    private Screen gameScreen;

    /**
     * Creates a new logic manager.
     */
    public ClientLogicManager() {
        renderer = new Renderer();
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        screenManager = new ScreenManager(this, renderer);

        try {
            client = new Client(renderer);
        } catch (final IOException e) {
            LOG.fatal("IO exception while creating client.", e);
            return;
        }
        LOG.info("Initializing renderer");

        final SettingsManager settings = SettingsManager.getInstance();

        renderer.initialize("WalledIn",
                settings.getInteger("engine.window.width"),
                settings.getInteger("engine.window.height"),
                settings.getBoolean("engine.window.fullScreen"));
        renderer.addListener(this);
        LOG.info("Starting renderer");
        renderer.beginLoop();
    }

    /**
     * Loads some textures.
     */
    private void loadTextures() {
        final TextureManager manager = TextureManager.getInstance();
        manager.loadFromURL(Utils.getClasspathURL("tiles.png"), "tiles");
        manager.loadFromURL(Utils.getClasspathURL("zon.png"), "sun");
        manager.loadFromURL(Utils.getClasspathURL("player.png"), "player");
        manager.loadFromURL(Utils.getClasspathURL("wall.png"), "wall");
    }

    /**
     * Returns the entity manager.
     * 
     * @return Entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Returns the entity factory.
     * 
     * @return Entity factory
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public void draw(final Renderer renderer) {
        screenManager.draw(renderer);

    }
    
    /**
     * Updates the client.
     */
    @Override
    public void update(final double delta) {
        /* Center the camera around the player */
        if (getManager().getPlayerName() != null) {
            final Entity player = getManager().getEntityManager().get(
                    getManager().getPlayerName());
            if (player != null) {
                getManager().getRenderer().centerAround(
                        (Vector2f) player.getAttribute(Attribute.POSITION));
            }
        }
    }

    /**
     * Gets called when the renderer is done initializing.
     */
    @Override
    public void initialize() {
        /* Prevent loading assets twice. */
        if (assetsLoaded) {
            return;
        }

        /* Load standard font */
        final Font font = new Font();
        final String fontName = SettingsManager.getInstance().getString(
                "game.font");
        font.readFromStream(Utils.getClasspathURL(fontName + ".font"));
        screenManager.addFont(fontName, font);

        final Screen serverListScreen = new ServerListScreen(screenManager);
        screenManager.addScreen(ScreenType.SERVER_LIST, serverListScreen);

        try {
            screenManager.getEntityFactory().loadScript(
                    Utils.getClasspathURL("entities/entities.groovy"));
            screenManager.getEntityFactory().loadScript(
                    Utils.getClasspathURL("entities/cliententities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
            dispose();
        }
        // initialize entity manager
        screenManager.getEntityManager().init();

        // create cursor, use the factory so it does not get added to the list
        final Entity cursor = screenManager.getEntityFactory().create(
                screenManager.getEntityManager(), Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        renderer.hideHardwareCursor();

        assetsLoaded = true;
        loadTextures();
        createTextureParts();

        getManager().getEntityManager().create(Family.BACKGROUND, "Background");

        /* Load music */
        if (Audio.getInstance().isEnabled()) {
            Audio.getInstance().loadOggSample("background1",
                    Utils.getClasspathURL("audio/Clausterphobia.ogg"));

            for (int i = 1; i < 5; i++) {
                Audio.getInstance().loadWaveSample("handgun" + i,
                        Utils.getClasspathURL("audio/handgun_" + i + ".wav"));
                Audio.getInstance().loadWaveSample("foamgun" + i,
                        Utils.getClasspathURL("audio/foamgun_" + i + ".wav"));
            }

            /* Play background music */
            Audio.getInstance().playSample("background1", new Vector2f(), true);
        }

        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen(screenManager);
        screenManager.addScreen(ScreenType.GAME, gameScreen);
        final Screen selectTeamScreen = new SelectTeamScreen(screenManager);
        screenManager.addScreen(ScreenType.SELECT_TEAM, selectTeamScreen);
        final Screen menuScreen = new MainMenuScreen(screenManager);
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.initialize();
        menuScreen.show();
    }

    @Override
    public void dispose() {
        client.dispose();
    }

    /**
     * Starts the application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        /* Load configuration */
        try {
            SettingsManager.getInstance().loadSettings(
                    Utils.getClasspathURL("settings.ini"));
        } catch (final IOException e) {
            LOG.error("Could not read configuration file.", e);
        }

        ClientLogicManager logicManager = new ClientLogicManager();
    }

    private void createTextureParts() {
        final TexturePartManager manager = TexturePartManager.getInstance();
        manager.createTexturePart("player_eyes", "player", new Rectangle(70,
                96, 20, 32));
        manager.createTexturePart("player_background", "player", new Rectangle(
                96, 0, 96, 96));
        manager.createTexturePart("player_body", "player", new Rectangle(0, 0,
                96, 96));
        manager.createTexturePart("player_background_foot", "player",
                new Rectangle(192, 64, 96, 32));
        manager.createTexturePart("player_foot", "player", new Rectangle(192,
                32, 96, 32));
        manager.createTexturePart("sun", "sun", new Rectangle(0, 0, 128, 128));
        manager.createTexturePart(
                "tile_empty",
                "tiles",
                createMapTextureRectangle(6, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_filled",
                "tiles",
                createMapTextureRectangle(1, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass_end_left",
                "tiles",
                createMapTextureRectangle(4, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass_end_right",
                "tiles",
                createMapTextureRectangle(5, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass",
                "tiles",
                createMapTextureRectangle(16, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_left_grass",
                "tiles",
                createMapTextureRectangle(19, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_left_mud",
                "tiles",
                createMapTextureRectangle(20, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_right_mud",
                "tiles",
                createMapTextureRectangle(21, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_left_grass",
                "tiles",
                createMapTextureRectangle(32, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_left_mud",
                "tiles",
                createMapTextureRectangle(36, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_right_mud",
                "tiles",
                createMapTextureRectangle(37, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_left_grass_end",
                "tiles",
                createMapTextureRectangle(48, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_mud",
                "tiles",
                createMapTextureRectangle(52, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
    }

    private Rectangle createMapTextureRectangle(final int tileNumber,
            final int tileNumPerLine, final int tileWidth, final int tileHeight) {
        return new Rectangle((tileNumber % 16 * tileWidth + 1), (tileNumber
                / 16 * tileHeight + 1), (tileWidth - 2), (tileHeight - 2));
    }

}
