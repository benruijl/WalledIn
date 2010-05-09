package walledin.game;

import walledin.engine.math.Rectangle;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.RenderPlayerBehavior;

public class Player extends Entity {
	public Player(final String name, final String texture) {
		super(name);
		
		setAttribute(Attribute.ORIENTATION, new Integer(1)); // start looking to the right
		
		float fScale = 0.5f; // FIXME
		setAttribute(Attribute.BOUNDING_BOX, new Rectangle(0, 0, 96 * fScale, 96 * fScale));
		
		addBehavior(new PlayerControlBehaviour(this));
		addBehavior(new RenderPlayerBehavior(this, texture));
		addBehavior(new PlayerAnimationBehavior(this));
	}
}

