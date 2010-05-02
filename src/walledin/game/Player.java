package walledin.game;

import walledin.engine.Rectangle;
import walledin.engine.Renderer;
import walledin.engine.Vector2f;

/**
 * 
 * @author ben
 */
public class Player {
	private static final float fScale = 0.5f;
	private String texID;
	private Vector2f pos;
	private final Rectangle boundRect;
	private float footPos; // for basic walking animation, in radians
	private boolean facingRight;

	public Rectangle getBoundRect() {
		return boundRect;
	}

	public Player(final String tex) {
		texID = tex;
		pos = new Vector2f();
		boundRect = new Rectangle(0, 0, 96 * fScale, 96 * fScale);
		facingRight = true;
	}

	public void Move(final float move) {
		pos.x += move;
		footPos += 0.4f;
		facingRight = move >= 0.0f;
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(final Vector2f pos) {
		this.pos = pos;
	}

	public String getTexID() {
		return texID;
	}

	public void setTexID(final String texID) {
		this.texID = texID;
	}

	public void draw(final Renderer renderer) {
		renderer.pushMatrix();
		renderer.translate(pos);
		renderer.scale(new Vector2f(fScale, fScale));
		if (!facingRight) {
			renderer.scale(new Vector2f(-1, 1));
		}

		renderer.drawRect("player", new Rectangle(96 / 256.0f, 0, 96 / 256.0f,
				96 / 128.0f), new Rectangle(0, 0, 96, 96)); // render background
		renderer.drawRect("player", new Rectangle(0, 0, 96 / 256.0f,
				96 / 128.0f), new Rectangle(0, 0, 96, 96)); // draw body
		renderer.drawRect("player", new Rectangle(70 / 256.0f, 96 / 128.0f,
				20 / 256.0f, 32 / 128.0f), new Rectangle(45, 30, 20, 32)); // eyes
		renderer.drawRect("player", new Rectangle(70 / 256.0f, 96 / 128.0f,
				20 / 256.0f, 32 / 128.0f), new Rectangle(55, 30, 20, 32)); // eyes

		// render foot
		renderer
				.drawRect("player", new Rectangle(192 / 256.0f, 64 / 128.0f,
						96 / 256.0f, 32 / 128.0f), new Rectangle(
						(float) (java.lang.Math.cos(footPos) * 8.0 + 35.0), 60,
						96, 32));

		renderer
				.drawRect("player", new Rectangle(192 / 256.0f, 32 / 128.0f,
						96 / 256.0f, 32 / 128.0f), new Rectangle(
						(float) (java.lang.Math.cos(footPos) * 8.0 + 35.0), 60,
						96, 32)); // foot

		renderer.popMatrix();
	}
}
