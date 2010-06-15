package walledin.game.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.game.EntityManager;
import walledin.game.entity.EntityFactory;
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
    /** Player name */
    private String playerName;

    /**
     * Creates a screen manager.
     */
    public ScreenManager() {
        screens = new ArrayList<Screen>();
        fonts = new HashMap<String, Font>();

        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    /**
     * Registers the name of the player. Useful when the player object is needed.
     * @param name Name of player
     */
    public void setPlayerName(String name) {
        playerName = name;
    }
    
    public String getPlayerName() {
        return playerName;
    } 
    
    
    /**
     * Adds font to shared list.
     * @param name Name of font
     * @param font Font object
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
}
