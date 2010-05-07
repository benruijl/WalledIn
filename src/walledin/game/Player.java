package walledin.game;

import walledin.engine.Rectangle;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.RenderPlayerBehavior;

/**
 * 
 * @author ben
 */
public class Player extends Entity {
	private final Rectangle boundRect;

	public Player(final String name, final String texture) {
		super(name);
		
		setAttribute(Attribute.ORIENTATION, new Integer(1)); // start looking right
		
		addBehavior(new PlayerControlBehaviour(this));
		addBehavior(new RenderPlayerBehavior(this, texture));
		addBehavior(new PlayerAnimationBehavior(this));
		
		
		float fScale = 0.5f;
		boundRect = new Rectangle(0, 0, 96 * fScale, 96 * fScale);
	}
	
	public Rectangle getBoundRect() {
		return boundRect;
	}
	
	public Vector2f getPosition()
	{
		return getAttribute(Attribute.POSITION);
	}

}

