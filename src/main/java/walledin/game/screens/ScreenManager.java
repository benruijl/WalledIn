package walledin.game.screens;

import java.util.HashMap;
import java.util.Map;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.game.EntityManager;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.network.client.Client;
import walledin.game.screens.Screen.ScreenState;

public class ScreenManager {
    
    /** Screen types.*/
    public enum ScreenType {
        MAIN_MENU, GAME, SERVER_LIST
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

    /**
     * Creates a screen manager.
     * 
     * @param renderer
     *            Renderer used by screen manager
     */
    public ScreenManager(final Client client, final Renderer renderer) {
        screens = new HashMap<ScreenType, Screen>();
        fonts = new HashMap<String, Font>();
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);

        this.client = client;
        this.renderer = renderer;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * Gets the renderer associated with all screens.
     * 
     * @return Renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Registers the name of the player. Useful when the player object is
     * needed.
     * 
     * @param name
     *            Name of player
     */
    public void setPlayerName(final String name) {
        playerName = name;
    }

    public String getPlayerName() {
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
    public void addFont(final String name, final Font font) {
        fonts.put(name, font);
    }

    /**
     * Gets one of the shared fonts.
     * 
     * @param name
     *            Name of the font
     * @return Font object if in list, else null.
     */
    public Font getFont(final String name) {
        return fonts.get(name);
    }

    /**
     * Adds screen to the list.
     * 
     * @param screen
     *            Screen to add
     */
    public void addScreen(final ScreenType type, final Screen screen) {
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
    public Screen getScreen(final ScreenType type) {
        return screens.get(type);
    }

    /**
     * Updates every screen.
     * 
     * @param delta
     *            Delta time
     */
    public void update(final double delta) {
        for (final Screen screen : screens.values()) {
            screen.update(delta);
        }

        // TODO: always update every screen or make selection?
    }

    /**
     * Draws every visible screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public void draw(final Renderer renderer) {
        for (final Screen screen : screens.values()) {
            if (screen.getState() == ScreenState.Visible) {
                screen.draw(renderer);
            }
        }
    }

    public Entity getCursor() {
        return cursor;
    }

    public void setCursor(final Entity cursor) {
        this.cursor = cursor;
    }

    /**
     * Kill the application. Sends the dispose message to the client.
     */
    public void dispose() {
        client.dispose();
    }
    
    /**
     * Get the client.
     * @return Client that owns this screen manager.
     */
    public Client getClient() {
        return client;
    }
}
