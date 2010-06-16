package walledin.game.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.game.EntityManager;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.network.client.Client;
import walledin.game.screens.Screen.ScreenState;

public class ScreenManager {
    /** List of screens */
    private List<Screen> screens;
    /** Entity list of all screens together */
    private final EntityManager entityManager;
    /** Client entity factory */
    private final EntityFactory entityFactory;
    /** Map of shared fonts */
    private final Map<String, Font> fonts;
    /** Shared cursor */
    private Entity cursor;
    /** Shared renderer */
    private final Renderer renderer;
    /** Player name */
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
        screens = new ArrayList<Screen>();
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
    public void setPlayerName(String name) {
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
    public void addFont(String name, Font font) {
        fonts.put(name, font);
    }

    /**
     * Gets one of the shared fonts.
     * 
     * @param name
     *            Name of the font
     * @return Font object if in list, else null.
     */
    public Font getFont(String name) {
        return fonts.get(name);
    }

    /**
     * Adds screen to the list.
     * 
     * @param screen
     *            Screen to add
     */
    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.registerScreenManager(this);
    }

    /**
     * Updates every screen.
     * 
     * @param delta
     *            Delta time
     */
    public void update(double delta) {
        for (Screen screen : screens)
            screen.update(delta);

        // TODO: always update every screen or make selection?
    }

    /**
     * Draws every visible screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public void draw(Renderer renderer) {
        for (Screen screen : screens)
            if (screen.getState() == ScreenState.Visible)
                screen.draw(renderer);
    }

    public Entity getCursor() {
        return cursor;
    }

    public void setCursor(Entity cursor) {
        this.cursor = cursor;
    }

    /**
     * Kill the application. Sends the dispose message to the client.
     */
    public void dispose() {
        client.dispose();
    }
}
