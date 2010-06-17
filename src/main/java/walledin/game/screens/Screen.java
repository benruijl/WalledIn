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
        Visible, Hidden
    }

    /** Manager of this screen */
    private ScreenManager manager;

    /** State of this screen */
    private ScreenState state;

    /** Active flag */
    protected boolean active;

    /**
     * To be called when screen is added to the list. Do not call on beforehand,
     * because some functions may require a parent screen manager.
     */
    abstract public void initialize();

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

    /**
     * Flag the screen as active/inactive. If activated, it will also make the
     * screen visible.
     * 
     * @param active
     *            Activate or deactivate
     */
    public void setActive(boolean active) {
        this.active = active;

        if (active)
            this.state = ScreenState.Visible;
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

    public ScreenManager getManager() {
        return manager;
    }
}
