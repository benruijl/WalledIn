package walledin.game.screens;

import java.awt.event.KeyEvent;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class GameScreen extends Screen {
    @Override
    public void draw(final Renderer renderer) {
        // prevent network from coming in between
        synchronized (getManager().getEntityManager()) {
            EntityManager entityManager = getManager().getEntityManager();
            
            entityManager.draw(renderer); // draw all entities in correct order

            /* Render current FPS */
            renderer.startHUDRendering();
            
            Font font = getManager().getFont("arial20"); 
            font.renderText(renderer, "FPS: " + renderer.getFPS(),
                    new Vector2f(600, 20));

            final Entity player = entityManager.get(getManager().getPlayerName());

            if (player != null) {
                font.renderText(renderer,
                        "HP: " + player.getAttribute(Attribute.HEALTH),
                        new Vector2f(600, 40));
            }

            renderer.stopHUDRendering();
        }

    }

    @Override
    public void update(double delta) {
        // prevent network from coming in between
        synchronized (getManager().getEntityManager()) {
            /* Update all entities */
            getManager().getEntityManager().update(delta);

            /* Center the camera around the player */
            if (getManager().getPlayerName() != null) {
                final Entity player = getManager().getEntityManager().get(getManager().getPlayerName());
                if (player != null) {
                    getManager().getRenderer().centerAround((Vector2f) player
                            .getAttribute(Attribute.POSITION));
                }
            }
        }

        /* Update cursor position */
        getManager().getCursor().setAttribute(Attribute.POSITION,
                getManager().getRenderer().screenToWorld(Input.getInstance().getMousePos()));

        /* Close the application */
        if (Input.getInstance().isKeyDown(KeyEvent.VK_ESCAPE)) {
            getManager().dispose();
            return;
        }

        /* Toggle full screen, current not working correctly */
        if (Input.getInstance().isKeyDown(KeyEvent.VK_F1)) {
            //renderer.toggleFullScreen();
            Input.getInstance().setKeyUp(KeyEvent.VK_F1);
        }

    }

    @Override
    public void initialize() {
        
    }

}
