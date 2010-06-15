package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;
import walledin.game.screens.Screen.ScreenState;

public class ScreenManager {
    /** List of screens */
    private List<Screen> screens;

    /**
     * Creates a screen manager.
     */
    public ScreenManager() {
        screens = new ArrayList<Screen>();
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
     * @param delta Delta time
     */
    public void update(double delta) {
        for (Screen screen : screens)
            screen.update(delta);
        
        // TODO: always update screen or make selection?
    }
    
    /**
     * Draws every visible screen.
     * @param renderer Renderer to draw with
     */
    public void draw(Renderer renderer) {
        for (Screen screen : screens)
            if (screen.getState() == ScreenState.Visible)
                screen.draw(renderer);
    }
}
