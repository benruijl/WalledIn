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
package walledin.game.gui;

import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.gui.Screen;
import walledin.engine.gui.ScreenKeyEvent;
import walledin.engine.gui.ScreenKeyEventListener;
import walledin.engine.gui.ScreenManager;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class GameScreen extends Screen implements ScreenKeyEventListener {
    private static final Logger LOG = Logger.getLogger(GameScreen.class);
    private final ClientLogicManager clientLogicManager;

    public GameScreen(final ScreenManager manager,
            final ClientLogicManager clientLogicManager) {
        super(manager, null, 0);
        addKeyEventListener(this);
        this.clientLogicManager = clientLogicManager;
    }

    @Override
    public void draw(final Renderer renderer) {
        // prevent network from coming in between
        final EntityManager entityManager = clientLogicManager
                .getEntityManager();

        renderer.applyCamera();
        entityManager.draw(renderer); // draw all entities in correct order

        renderer.startHUDRendering();
        final Font font = getManager().getFont("arial20");
        final Entity player = entityManager.get(clientLogicManager
                .getPlayerName());

        if (player != null) {
            font.renderText(renderer,
                    "HP: " + player.getAttribute(Attribute.HEALTH),
                    new Vector2f(630, 40));
        }

        renderer.stopHUDRendering();

        super.draw(renderer);
    }

    @Override
    public void update(final double delta) {
        super.update(delta);

        /* Center the camera around the player */
        if (clientLogicManager.getPlayerName() != null) {
            final Entity player = clientLogicManager.getEntityManager().get(
                    clientLogicManager.getPlayerName());
            if (player != null) {
                getManager().getRenderer().centerAround(
                        (Vector2f) player.getAttribute(Attribute.POSITION));
            }
        }

        /*
         * If no other screen has the focus and this window is visible, take the
         * focus.
         */
        if (getManager().getFocusedScreen() == null) {
            getManager().setFocusedScreen(this);
        }
    }

    @Override
    public void onKeyDown(final ScreenKeyEvent e) {
        if (e.getKeys().contains(KeyEvent.VK_ESCAPE)) {
            Input.getInstance().setKeyUp(KeyEvent.VK_ESCAPE);

            getManager().getScreen(ScreenType.SERVER_LIST).show();
            hide();
        }

    }

    @Override
    public void initialize() {
    }

}
