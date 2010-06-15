package walledin.game.screens;

import walledin.engine.Renderer;

/**
 * A game screen. Can be anything from a background to a pop-up dialog.
 * 
 * @author Ben Ruijl
 * 
 */
public abstract class Screen {
    public enum ScreenState {
        Visible,
        Hidden 
    }
    
    /** Manager of this screen */
    private ScreenManager manager;
    
    /** State of this screen */
    private ScreenState state;

    /** Active flag */
    protected boolean active;

    /**
     * Updates the screen.
     * 
     * @param delta
     *            Delta time since last update
     */
    abstract public void update(double delta);

    /**
     * Draws the screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    abstract public void draw(Renderer renderer);

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public ScreenState getState() {
        return state;
    }
    
    public void setState(ScreenState state) {
        this.state = state;
    }

    /**
     * Links the screen manager to this screen. This function is usually called
     * by the screen manager on adding the screen.
     * 
     * @param manager
     *            Screen manager
     */
    public void registerScreenManager(final ScreenManager manager) {
        this.manager = manager;
    }
}
