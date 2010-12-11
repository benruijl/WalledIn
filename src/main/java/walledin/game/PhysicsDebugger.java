package walledin.game;

import org.apache.log4j.Logger;

import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.physics.DebugDrawer;
import walledin.engine.physics.PhysicsManager;

public class PhysicsDebugger implements RenderListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(PhysicsDebugger.class);
    /** Debug renderer. */
    private final Renderer renderer = new Renderer();
    /** Debug drawer. */
    private final DebugDrawer debugDrawer;

    /**
     * Do not create before the world has been created!
     */
    public PhysicsDebugger() {
        renderer.initialize("Debug", 800, 600, false);
        renderer.addListener(this);

        debugDrawer = new DebugDrawer(renderer);

        PhysicsManager.getInstance().registerDebugDrawer(debugDrawer);

        /* Sets the debug mode to drawing wireframe models. */
        debugDrawer.setDebugMode(2);

        renderer.beginLoop();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void draw(Renderer renderer) {
        PhysicsManager.getInstance().getWorld().debugDrawWorld();
    }

    @Override
    public void dispose() {
    }

}
