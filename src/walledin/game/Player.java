package walledin.game;

import walledin.engine.Rectangle;
import walledin.engine.Renderer;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.PositionBehavior;
import walledin.game.entity.behaviors.RenderBehavior;
import walledin.game.entity.behaviors.RenderPlayerBehavior;

/**
 * 
 * @author ben
 */
public class Player extends Entity {
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
		super("Player");
		
		addBehavior(new RenderPlayerBehavior());
		setAttribute(Attribute.POSITION, new Vector2f());
			
		texID = tex;
		
		boundRect = new Rectangle(0, 0, 96 * fScale, 96 * fScale);
		facingRight = true;
	}

	public void Move(final float move) {
		//Vector2f oldpos = getAttribute()
		//setPos(getPos().add(new Vector2f(move, 0)));
		footPos += 0.4f;
		facingRight = move >= 0.0f;
	}

	public String getTexID() {
		return texID;
	}

	public void setTexID(final String texID) {
		this.texID = texID;
	}
}
