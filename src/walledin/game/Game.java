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

/**
 * 
 * @author ben
 */
public class Game implements RenderListener {
	private List<Rectangle> walls;
	private Map<String, Entity> entities;
	private DrawOrderManager drawOrder;

	public Game() {
		entities = new LinkedHashMap<String, Entity>();
		walls = new ArrayList<Rectangle>();
	}

	public void update(final double delta) {
		/* Update all entities */
		for (Entity ent : entities.values()) {
			ent.sendUpdate(delta);
		}

		Vector2f vNewPos = entities.get("Player01").getAttribute(
				Attribute.POSITION);

		if (Input.getInstance().keyDown(KeyEvent.VK_SPACE)) {
			walls
					.add(new Rectangle(vNewPos.getX() + 65, vNewPos.getY(), 30,
							90));
			Input.getInstance().setKeyUp(KeyEvent.VK_SPACE);
		}

	}

	public void draw(final Renderer renderer) {
		renderer.drawRect("sun", new Rectangle(60, 60, 64, 64));

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
	 * Initialise game
	 */
	public void init() {
		entities = new LinkedHashMap<String, Entity>();
		drawOrder = new DrawOrderManager();

		loadTextures();
		createTextureParts();

		GameMapIO mMapIO = new GameMapIOXML(); // choose XML as format

		entities.put("Map", mMapIO.readFromFile("data/map.xml"));
		entities.put("Player01", new Player("Player01", "player", "player_eyes"));
		entities.get("Player01").setAttribute(Attribute.POSITION,
				new Vector2f(10, 10));
		drawOrder.add(entities.values()); // add to draw list
	}

	private void loadTextures() {
		TextureManager manager = TextureManager.getInstance();
		manager.loadFromFile("data/tiles.png", "tiles");
		manager.loadFromFile("data/zon.png", "sun");
		manager.loadFromFile("data/player.png", "player");
		manager.loadFromFile("data/wall.png", "wall");
		manager.loadFromFile("data/game.png", "game");
	}

	private void createTextureParts() {
		TexturePartManager manager = TexturePartManager.getInstance();
		manager.createTexturePart("player_eyes", "player", new Rectangle(
				70 / 256.0f, 96 / 128.0f, 20 / 256.0f, 32 / 128.0f));
	}
}
