/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.game;

import java.awt.event.KeyEvent;
import java.util.Vector;

import walledin.engine.Input;
import walledin.engine.Rectangle;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.Vector2f;

/**
 * 
 * @author ben
 */
public class Game implements RenderListener {

	float fX;
	GameMapIO mMapIO;
	GameMap mMap;
	Player mPlayer;
	Vector<Rectangle> mWalls;

	public Game() {
	}

	public void update(final int ndt) {
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
		if (java.lang.Math.abs(vNewPos.x - mPlayer.getPos().x) > 0.1f) {
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

		mPlayer.draw(renderer);

		for (int i = 0; i < mWalls.size(); i++) {
			renderer.drawRect("wall", new Rectangle(0.0f, 0.0f, 110 / 128.0f,
					235 / 256.0f), mWalls.get(i));
		}

		renderer.centerAround(mPlayer.getPos()); // FIXME: do somewhere else
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

		mWalls = new Vector<Rectangle>();
	}
}
