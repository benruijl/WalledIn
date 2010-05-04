package walledin.game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import walledin.engine.Input;
import walledin.engine.Rectangle;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.Vector2f;
import walledin.game.entity.MessageType;

/**
 * 
 * @author ben
 */
public class Game implements RenderListener {

	private float fX;
	private GameMapIO mMapIO;
	private GameMap mMap;
	private Player mPlayer;
	private List<Rectangle> mWalls;

	public void update(final double delta) {
		final float moveSpeed = 4.0f;
		final Vector2f gravity = new Vector2f(0, 2.0f);

		Vector2f vNewPos = mPlayer.getPos();

		vNewPos = vNewPos.add(gravity); // do gravity

		if (Input.getInstance().keyDown(KeyEvent.VK_RIGHT)) {
			vNewPos.x += moveSpeed;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_LEFT)) {
			vNewPos.x -= moveSpeed;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_UP)) {
			vNewPos.y -= moveSpeed;
		}

		if (Input.getInstance().keyDown(KeyEvent.VK_DOWN)) {
			vNewPos.y += moveSpeed;
		}

		/* Do very basic collision detection */
		for (int i = 0; i < mWalls.size(); i++) {
			if (mPlayer.getBoundRect().addOffset(vNewPos).intersects(
					mWalls.get(i))) {
				vNewPos = mPlayer.getPos(); // do no update
			}
		}
		/* Update player position */
		if (Math.abs(vNewPos.x - mPlayer.getPos().x) > 0.1f) {
			mPlayer.Move(vNewPos.x - mPlayer.getPos().x); // FIXME, used only
			// for foot
			// animation
		}
		mPlayer.setPos(vNewPos);

		if (Input.getInstance().keyDown(KeyEvent.VK_SPACE)) {
			mWalls.add(new Rectangle(mPlayer.getPos().x() + 65, mPlayer
					.getPos().y(), 30, 90));
			Input.getInstance().setKeyUp(KeyEvent.VK_SPACE);
		}

	}

	public void draw(final Renderer renderer) {
		renderer.drawRect("sun", new Rectangle(60, 60, 64, 64)); // draw
		// background

		if (mMap != null) {
			mMap.draw(renderer);
		}

		mPlayer.onMessage(MessageType.RENDER, renderer); // render player

		for (int i = 0; i < mWalls.size(); i++) {
			renderer.drawRect("wall", new Rectangle(0.0f, 0.0f, 110 / 128.0f,
					235 / 256.0f), mWalls.get(i));
		}

		/* FIXME: move these lines */
		renderer.centerAround(mPlayer.getPos());

		if (Input.getInstance().keyDown(KeyEvent.VK_F1))
		{
			renderer.toggleFullScreen();
			Input.getInstance().setKeyUp(KeyEvent.VK_F1);
		}
			
	}

	/**
	 * Init game
	 * 
	 */
	public void init() {
		TextureManager.getInstance().LoadFromFile("data/tiles.png", "tiles");
		TextureManager.getInstance().LoadFromFile("data/zon.png", "sun");
		TextureManager.getInstance().LoadFromFile("data/player.png", "player");
		TextureManager.getInstance().LoadFromFile("data/wall.png", "wall");
		TextureManager.getInstance().LoadFromFile("data/game.png", "game");

		mMapIO = new GameMapIOXML(); // choose XML as format
		mMap = mMapIO.readFromFile("data/map.xml"); // create map

		mPlayer = new Player("player");
		mPlayer.setPos(new Vector2f(10, 10));

		mWalls = new ArrayList<Rectangle>();
	}
}
