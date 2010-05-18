package walledin.network;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.game.Background;
import walledin.game.CollisionManager;
import walledin.game.DrawOrderManager;
import walledin.game.Item;
import walledin.game.ItemFactory;
import walledin.game.Player;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMap;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class Client {
	private static final int TILE_SIZE = 64;
	private static final int TILES_PER_LINE = 16;
	private Map<String, Entity> entities;
	private DrawOrderManager drawOrder;
	private Font font;

	public Client() {
		entities = new LinkedHashMap<String, Entity>();
	}

	public void update(final double delta) {
		/* Update all entities */
		for (final Entity entity : entities.values()) {
			entity.sendUpdate(delta);
		}

		/* Do collision detection */
		CollisionManager.calculateMapCollisions((GameMap) entities.get("Map"),
				entities.values(), delta);
		CollisionManager.calculateEntityCollisions(entities.values(), delta);

		for (final Entity entity : entities.values()) {
			if (entity.isMarkedRemoved()) {
				removeEntity(entity.getName());
			}
		}
	}

	public void draw(final Renderer renderer) {
		drawOrder.draw(renderer); // draw all entities in correct order

		/* Render current FPS */
		renderer.startHUDRendering();
		font.renderText(renderer, "FPS: " + Float.toString(renderer.getFPS()), new Vector2f(600, 20));


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

		loadTextures();
		createTextureParts();

		font = new Font(); // load font
		font.readFromFile("data/arial20.font");

		// load all item information
		ItemFactory.getInstance().loadFromXML("data/items.xml");

		final GameMapIO mMapIO = new GameMapIOXML(); // choose XML as format

		entities.put("Map", mMapIO.readFromFile("data/map.xml"));
		entities.put("Background", new Background("Background"));
		entities.put("Player01", new Player("Player01"));
		entities.get("Player01").setAttribute(Attribute.POSITION,
				new Vector2f(400, 300));

		// add map items like healthkits to entity list
		final List<Item> mapItems = entities.get("Map").getAttribute(
				Attribute.ITEM_LIST);
		for (final Item item : mapItems) {
			entities.put(item.getName(), item);
		}

		drawOrder.add(entities.values()); // add to draw list
	}

	public Entity removeEntity(final String name) {
		final Entity entity = entities.remove(name);
		drawOrder.removeEntity(entity);
		return entity;
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
