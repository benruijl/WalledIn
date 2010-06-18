package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;

/**
 * A game screen. Can be anything from a background to a pop-up dialog.
 * 
 * @author Ben Ruijl
 * 
 */
public abstract class Screen {
    /** Screen states. */
    public enum ScreenState {
        Visible, Hidden
    }

    /** Parent of this screen. */
    private final Screen parent;

    /** Child screens of this screen. */
    private List<Screen> children;

    /** Manager of this screen. */
    private ScreenManager manager;

    /** State of this screen. */
    private ScreenState state;

    /** Active flag. */
    protected boolean active;

    /**
     * Creates a new screen.
     * 
     * @param parent
     *            Parent of the screen or null of there is no parent.
     */
    public Screen(final Screen parent) {
        children = new ArrayList<Screen>();
        this.parent = parent;
    }

    /**
     * To be called when screen is added to the list. Do not call on beforehand,
     * because some functions may require a parent screen manager.
     */
    public abstract void initialize();

    /**
     * Updates the screen.
     * 
     * @param delta
     *            Delta time since last update
     */
    public void update(double delta) {
        for (Screen screen : children) {
            screen.update(delta);
        }
    }

    /**
     * Draws the screen.
     * 
     * @param renderer
     *            Renderer to draw with
     */
    public void draw(Renderer renderer) {
        for (Screen screen : children) {
            screen.draw(renderer);
        }
    }

    public final boolean isActive() {
        return active;
    }

    /**
     * Flag the screen as active/inactive. If activated, it will also make the
     * screen visible.
     * 
     * @param active
     *            Activate or deactivate
     */
    public final void setActive(final boolean active) {
        this.active = active;

        if (active) {
            state = ScreenState.Visible;
        }
    }

    public final ScreenState getState() {
        return state;
    }

    public final void setState(final ScreenState state) {
        this.state = state;
    }

    /**
     * Links the screen manager to this screen. This function is usually called
     * by the screen manager on adding the screen.
     * 
     * @param manager
     *            Screen manager
     */
    public final void registerScreenManager(final ScreenManager manager) {
        this.manager = manager;
    }

    public final ScreenManager getManager() {
        return manager;
    }

    public void addChild(final Screen sc) {
        children.add(sc);
        sc.registerScreenManager(getManager());
    }

}
