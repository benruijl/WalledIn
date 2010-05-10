package walledin.game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;

/**
 * 
 * @author ben
 */
public class Game implements RenderListener {
	private static final int TILE_SIZE = 64;
	private static final int TILES_PER_LINE = 16;
	private final List<Rectangle> walls;
	private Map<String, Entity> entities;
	private DrawOrderManager drawOrder;

	public Game() {
		entities = new LinkedHashMap<String, Entity>();
		walls = new ArrayList<Rectangle>();
	}

	public void update(final double delta) {
		/* Update all entities */
		for (final Entity ent : entities.values()) {
			ent.sendUpdate(delta);
		}

		final Vector2f vNewPos = entities.get("Player01").getAttribute(
				Attribute.POSITION);

		if (Input.getInstance().keyDown(KeyEvent.VK_SPACE)) {
			walls
					.add(new Rectangle(vNewPos.getX() + 65, vNewPos.getY(), 30,
							90));
			Input.getInstance().setKeyUp(KeyEvent.VK_SPACE);
		}

	}

	public void draw(final Renderer renderer) {
		drawOrder.draw(renderer); // draw all entities in correct order

		for (int i = 0; i < walls.size(); i++) {
			renderer.drawRect("wall", new Rectangle(0.0f, 0.0f, 110 / 128.0f,
					235 / 256.0f), walls.get(i));
		}

		/* FIXME: move these lines */
		renderer.centerAround((Vector2f) entities.get("Player01").getAttribute(
				Attribute.POSITION));

		if (Input.getInstance().keyDown(KeyEvent.VK_F1)) {
			renderer.toggleFullScreen();
			Input.getInstance().setKeyUp(KeyEvent.VK_F1);
		}
	}

	/**
	 * Initialize game
	 */
	public void init() {
		entities = new LinkedHashMap<String, Entity>();
		drawOrder = new DrawOrderManager();

		final long time = System.nanoTime();
        loadTextures();
        double diff = System.nanoTime() - time;
        diff /= 1000000000;
        System.out.println(diff);
		
		
		createTextureParts();

		final GameMapIO mMapIO = new GameMapIOXML(); // choose XML as format

		entities.put("Map", mMapIO.readFromFile("data/map.xml"));
		entities.put("Background", new Background("Background"));
		entities.put("Player01", new Player("Player01"));
		entities.get("Player01").setAttribute(Attribute.POSITION,
				new Vector2f(10, 10));
		
		final ItemFactory fac = new ItemFactory();
		fac.loadFromXML("data/items.xml"); // load all item information
		
		final Item hk = fac.create("healthkit", "healthkit01");
		entities.put(hk.getName(), hk);
		
		drawOrder.add(entities.values()); // add to draw list
	}

	private void loadTextures() {
		final TextureManager manager = TextureManager.getInstance();
		manager.loadFromFile("data/tiles.png", "tiles");
		manager.loadFromFile("data/zon.png", "sun");
		manager.loadFromFile("data/player.png", "player");
		manager.loadFromFile("data/wall.png", "wall");
		manager.loadFromFile("data/game.png", "game");
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
				createMapTextureRectangle(0, TILES_PER_LINE, TILE_SIZE,
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
