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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.audio.Audio;
import walledin.engine.gui.Screen;
import walledin.engine.gui.ScreenManager;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.gui.GameScreen;
import walledin.game.gui.MainMenuScreen;
import walledin.game.gui.SelectTeamScreen;
import walledin.game.gui.ServerListScreen;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.Tile;
import walledin.game.network.client.Client;
import walledin.util.SettingsManager;
import walledin.util.Utils;

/**
 * The logic manager for the client.
 * 
 * @author Ben Ruijl
 * 
 */
public final class ClientLogicManager implements RenderListener,
        EntityUpdateListener {
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
    /**
     * Game dependent assets. These should be removed when the game is finished.
     */
    private final List<Entity> gameAssets;
    /** Screen manager. */
    private final ScreenManager screenManager;
    /** Renderer. */
    private final Renderer renderer;
    /** Network client. */
    private final Client client;
    /** The screen of the game (not the menus). */
    private Screen gameScreen;
    /** Is the client quitting? */
    private boolean quitting = false;
    /** The name of the player of this client. */
    private String playerName;

    /**
     * Creates a new logic manager.
     * 
     * @throws WalledInException
     *             A generic error
     */
    public ClientLogicManager() throws WalledInException {
        renderer = new Renderer();
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        entityManager.setListener(this);
        screenManager = new ScreenManager(renderer);
        gameAssets = new ArrayList<Entity>();

        try {
            client = new Client(renderer, this);
        } catch (final IOException e) {
            LOG.fatal("IO exception while creating client.", e);
            throw new WalledInException("Could not initialize the client.", e);
        }
        LOG.info("Initializing renderer");

        final SettingsManager settings = SettingsManager.getInstance();

        renderer.initialize("WalledIn",
                settings.getInteger("engine.window.width"),
                settings.getInteger("engine.window.height"),
                settings.getBoolean("engine.window.fullScreen"));
        renderer.addListener(this);
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

        /* Show FPS for debugging */
        renderer.startHUDRendering();
        final Font font = screenManager.getFont("arial20");
        font.renderText(renderer, "FPS: " + renderer.getFPS(), new Vector2f(
                630, 20));
        renderer.stopHUDRendering();
    }

    /**
     * Starts the render loop
     */
    public void start() {
        LOG.info("Starting renderer");
        renderer.beginLoop();
    }

    /**
     * Registers the name of the player entity. Useful when the player entity is
     * needed.
     * 
     * @param name
     *            Name of player
     */
    public void setPlayerName(final String name) {
        playerName = name;
    }

    /**
     * Gets the player entity name.
     * 
     * @return Player entity name
     */
    public String getPlayerName() {
        return playerName;
    }

    public Screen getGameScreen() {
        return gameScreen;
    }

    public Client getClient() {
        return client;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public ScreenManager getScreenManager() {
        return screenManager;
    }

    public List<Entity> getGameAssets() {
        return gameAssets;
    }

    /**
     * Called when a new game entity is created. These entities are stored in a
     * separate list.
     * 
     * @param entity
     *            Game entity
     */
    @Override
    public void entityCreated(final Entity entity) {
        gameAssets.add(entity);

        /* Play a sound when a bullet is created */
        final Random generator = new Random();
        final int num = generator.nextInt(4) + 1;

        if (Audio.getInstance().isEnabled()) {
            if (entity.getFamily() == Family.HANDGUN_BULLET) {
                Audio.getInstance().playSample("handgun" + num, new Vector2f(),
                        false);
            }

            if (entity.getFamily() == Family.FOAMGUN_BULLET) {
                Audio.getInstance().playSample("foamgun" + num, new Vector2f(),
                        false);
            }
        }

        if (entity.getFamily() == Family.MAP) {
            final GameMapIO mapIO = new GameMapIOXML();
            final String mapName = (String) entity
                    .getAttribute(Attribute.MAP_NAME);
            if (mapName != null) {
                final List<Tile> tiles = mapIO.readTilesFromURL(Utils
                        .getClasspathURL(mapName));
                entity.setAttribute(Attribute.TILES, tiles);
            } else {
                LOG.warn("map name is null!");
            }
        }
    }

    /**
     * Called when an entity gets removed from the game.
     * 
     * @param entity
     *            Game entity
     */
    @Override
    public void entityRemoved(final Entity entity) {
        gameAssets.remove(entity);
    }

    /**
     * Resets the game by removing assets and by resetting other values.
     */
    public void resetGame() {
        LOG.info("Cleaning up game assets.");

        /* Remove the game assets. */
        client.resetReceivedVersion();

        for (final Entity asset : gameAssets) {
            entityManager.remove(asset.getName());
        }
    }

    /**
     * Displays an error message, disconnects from the server and returns to the
     * server list.
     * 
     * @param message
     *            Message to display
     */
    public void displayErrorAndDisconnect(final String message) {
        screenManager.createDialog(message);
        LOG.error(message);

        resetGame();
        try {
            client.disconnectFromServer();
        } catch (final IOException e) {
            LOG.error("Error while trying to disconnect from server", e);
        }

        screenManager.getScreen(ScreenType.SERVER_LIST).show();
        screenManager.getScreen(ScreenType.GAME).hide();
    }

    /**
     * Updates the client.
     */
    @Override
    public void update(final double delta) {
        /* Only register actions when the game screen has the focus. */
        if (screenManager.getFocusedScreen() != screenManager
                .getScreen(ScreenType.GAME)) {
            PlayerActionManager.getInstance().clear();
        } else {
            PlayerActionManager.getInstance().update();
        }

        /* Update all entities */
        getEntityManager().update(delta);

        client.update(delta);

        if (Audio.getInstance().isEnabled()) {
            Audio.getInstance().update();
        }

        screenManager.update(delta);
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

        final Screen serverListScreen = new ServerListScreen(screenManager,
                this);
        screenManager.addScreen(ScreenType.SERVER_LIST, serverListScreen);

        try {
            entityFactory.loadScript(Utils
                    .getClasspathURL("entities/entities.groovy"));
            entityFactory.loadScript(Utils
                    .getClasspathURL("entities/cliententities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
            dispose();
        }
        // initialize entity manager
        entityManager.init();

        // create cursor, use the factory so it does not get added to the list
        final Entity cursor = entityFactory.create(entityManager,
                Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        renderer.hideHardwareCursor();

        assetsLoaded = true;
        loadTextures();
        createTextureParts();

        entityManager.create(Family.BACKGROUND, "Background");

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
        gameScreen = new GameScreen(screenManager, this);
        screenManager.addScreen(ScreenType.GAME, gameScreen);
        final Screen selectTeamScreen = new SelectTeamScreen(screenManager,
                this);
        screenManager.addScreen(ScreenType.SELECT_TEAM, selectTeamScreen);
        final Screen menuScreen = new MainMenuScreen(screenManager, this);
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.initialize();
        menuScreen.show();
    }

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();
            client.dispose();
        }
    }

    /**
     * Starts the application.
     * 
     * @param args
     * @throws WalledInException
     */
    public static void main(final String[] args) throws WalledInException {
        /* Load configuration */
        try {
            SettingsManager.getInstance().loadSettings(
                    Utils.getClasspathURL("settings.ini"));
        } catch (final IOException e) {
            LOG.error("Could not read configuration file.", e);
        }

        final ClientLogicManager logicManager = new ClientLogicManager();
        logicManager.start();
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
