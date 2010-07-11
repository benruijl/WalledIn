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
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.gui.ScreenManager.ScreenType;
import walledin.util.Utils;

public class GameScreen extends Screen {
    private static final Logger LOG = Logger.getLogger(GameScreen.class);

    private static final int TILE_SIZE = 64;
    private static final int TILES_PER_LINE = 16;

    public GameScreen() {
        super(null, null);
    }

    @Override
    public void draw(final Renderer renderer) {
        // prevent network from coming in between
        synchronized (getManager().getEntityManager()) {
            final EntityManager entityManager = getManager().getEntityManager();

            entityManager.draw(renderer); // draw all entities in correct order

            /* Render current FPS */
            renderer.startHUDRendering();

            final Font font = getManager().getFont("arial20");
            font.renderText(renderer, "FPS: " + renderer.getFPS(),
                    new Vector2f(600, 20));

            final Entity player = entityManager.get(getManager()
                    .getPlayerName());

            if (player != null) {
                font.renderText(renderer, "HP: "
                        + player.getAttribute(Attribute.HEALTH), new Vector2f(
                        600, 40));
            }

            renderer.stopHUDRendering();

            super.draw(renderer);
        }

    }

    @Override
    public void update(final double delta) {
        super.update(delta);

        /* Center the camera around the player */
        if (getManager().getPlayerName() != null) {
            final Entity player = getManager().getEntityManager().get(
                    getManager().getPlayerName());
            if (player != null) {
                getManager().getRenderer().centerAround(
                        (Vector2f) player.getAttribute(Attribute.POSITION));
            }
        }

        if (Input.getInstance().isKeyDown(KeyEvent.VK_ESCAPE)) {
            Input.getInstance().setKeyUp(KeyEvent.VK_ESCAPE);
            
            //reset camera when returning to menu
            getManager().getRenderer().getCamera().setPos(new Vector2f());
            
            getManager().getScreen(ScreenType.SERVER_LIST).show();
            hide();
        }
    }

    @Override
    public void initialize() {
        loadTextures();
        createTextureParts();

        getManager().getEntityManager().create(Family.BACKGROUND, "Background");
    }

    private void loadTextures() {
        final TextureManager manager = TextureManager.getInstance();
        manager.loadFromURL(Utils.getClasspathURL("tiles.png"), "tiles");
        manager.loadFromURL(Utils.getClasspathURL("zon.png"), "sun");
        manager.loadFromURL(Utils.getClasspathURL("player.png"), "player");
        manager.loadFromURL(Utils.getClasspathURL("wall.png"), "wall");
    }

    private void createTextureParts() {
        final TexturePartManager manager = TexturePartManager.getInstance();
        manager.createTexturePart("player_eyes", "player", new Rectangle(70,
                96, 20, 32));
        manager.createTexturePart("player_background", "player", new Rectangle(
                96, 0, 96, 96));
        manager.createTexturePart("player_body", "player", new Rectangle(0, 0,
                96, 96));
        manager.createTexturePart("player_background_foot", "player",
                new Rectangle(192, 64, 96, 32));
        manager.createTexturePart("player_foot", "player", new Rectangle(192,
                32, 96, 32));
        manager.createTexturePart("sun", "sun", new Rectangle(0, 0, 128, 128));
        manager.createTexturePart("tile_empty", "tiles",
                createMapTextureRectangle(6, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_filled", "tiles",
                createMapTextureRectangle(1, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_top_grass_end_left", "tiles",
                createMapTextureRectangle(4, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_top_grass_end_right", "tiles",
                createMapTextureRectangle(5, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_top_grass", "tiles",
                createMapTextureRectangle(16, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_left_grass", "tiles",
                createMapTextureRectangle(19, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_left_mud", "tiles",
                createMapTextureRectangle(20, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_right_mud", "tiles",
                createMapTextureRectangle(21, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_top_left_grass", "tiles",
                createMapTextureRectangle(32, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_bottom_left_mud", "tiles",
                createMapTextureRectangle(36, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_bottom_right_mud", "tiles",
                createMapTextureRectangle(37, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_top_left_grass_end", "tiles",
                createMapTextureRectangle(48, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart("tile_bottom_mud", "tiles",
                createMapTextureRectangle(52, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
    }

    private Rectangle createMapTextureRectangle(final int tileNumber,
            final int tileNumPerLine, final int tileWidth, final int tileHeight) {
        return new Rectangle((tileNumber % 16 * tileWidth + 1), (tileNumber
                / 16 * tileHeight + 1), (tileWidth - 2), (tileHeight - 2));
    }

}
