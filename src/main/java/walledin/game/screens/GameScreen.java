package walledin.game.screens;

import java.awt.event.KeyEvent;

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
import walledin.util.Utils;

public class GameScreen extends Screen {
    private static final int TILE_SIZE = 64;
    private static final int TILES_PER_LINE = 16;
    
     public GameScreen(Screen parent) {
        super(parent);
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
        
        // prevent network from coming in between
        synchronized (getManager().getEntityManager()) {
            /* Update all entities */
            getManager().getEntityManager().update(delta);

            /* Center the camera around the player */
            if (getManager().getPlayerName() != null) {
                final Entity player = getManager().getEntityManager().get(
                        getManager().getPlayerName());
                if (player != null) {
                    getManager().getRenderer().centerAround(
                            (Vector2f) player.getAttribute(Attribute.POSITION));
                }
            }
        }

        /* Update cursor position */
        getManager().getCursor().setAttribute(
                Attribute.POSITION,
                getManager().getRenderer().screenToWorld(
                        Input.getInstance().getMousePos()));

        /* Close the application */
        if (Input.getInstance().isKeyDown(KeyEvent.VK_ESCAPE)) {
            getManager().dispose();
            return;
        }

        /* Toggle full screen, current not working correctly */
        if (Input.getInstance().isKeyDown(KeyEvent.VK_F1)) {
            // renderer.toggleFullScreen();
            Input.getInstance().setKeyUp(KeyEvent.VK_F1);
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
