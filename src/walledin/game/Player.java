package walledin.game;

import walledin.engine.Rectangle;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.RenderPlayerBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;

/**
 * 
 * @author ben
 */
public class Player extends Entity {
	private static final float fScale = 0.5f;
	private final Rectangle boundRect;
	private float footPos; // for basic walking animation, in radians
	private boolean facingRight;

	public Player(final String name, final String texture) {
		super(name);
		
		addBehavior(new PlayerControlBehaviour());
		addBehavior(new RenderPlayerBehavior(texture));
		setAttribute(Attribute.POSITION, new Vector2f());
		
		boundRect = new Rectangle(0, 0, 96 * fScale, 96 * fScale);
		facingRight = true;
	}
	
	public Rectangle getBoundRect() {
		return boundRect;
	}
	
	public Vector2f getPosition()
	{
		return getAttribute(Attribute.POSITION);
	}

	public void move(final float move) {
		//Vector2f oldpos = getAttribute()
		//setPos(getPos().add(new Vector2f(move, 0)));
		footPos += 0.4f;
		facingRight = move >= 0.0f;
	}
}
