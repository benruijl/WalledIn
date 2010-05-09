package walledin.game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import walledin.engine.Input;
import walledin.engine.Rectangle;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

/**
 * 
 * @author ben
 */
public class Game implements RenderListener {
	private List<Rectangle> walls;
	private Map<String, Entity> entities;

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
			walls.add(new Rectangle(vNewPos.x() + 65, vNewPos.y(), 30, 90));
			Input.getInstance().setKeyUp(KeyEvent.VK_SPACE);
		}

	}

	public void draw(final Renderer renderer) {
		renderer.drawRect("sun", new Rectangle(60, 60, 64, 64));

		/* Draw all entities */
		for (Entity entity : entities.values()) {
			entity.sendMessage(MessageType.RENDER, renderer);
		}

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
		TextureManager.getInstance().loadFromFile("data/tiles.png", "tiles");
		TextureManager.getInstance().loadFromFile("data/zon.png", "sun");
		TextureManager.getInstance().loadFromFile("data/player.png", "player");
		TextureManager.getInstance().loadFromFile("data/wall.png", "wall");
		TextureManager.getInstance().loadFromFile("data/game.png", "game");

		GameMapIO mMapIO = new GameMapIOXML(); // choose XML as format

		entities.put("Map", mMapIO.readFromFile("data/map.xml"));
		entities.put("Player01", new Player("Player01", "player"));
		entities.get("Player01").setAttribute(Attribute.POSITION,
				new Vector2f(10, 10));
	}
}
