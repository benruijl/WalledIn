package walledin.game;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.HealthBehavior;
import walledin.game.entity.behaviors.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.PlayerControlBehaviour;
import walledin.game.entity.behaviors.PlayerRenderBehavior;
import walledin.math.Rectangle;

public class Player extends Entity {
	public Player(final String name) {
		super(name, "player");

		setAttribute(Attribute.ORIENTATION, 1); // start looking to
		// the right

		addBehavior(new HealthBehavior(this, 100, 100));
		addBehavior(new PlayerControlBehaviour(this));
		addBehavior(new PlayerRenderBehavior(this));
		addBehavior(new PlayerAnimationBehavior(this));

		// FIXME correct the drawing instead of the hack the bounding box
		setAttribute(Attribute.BOUNDING_RECT, new Rectangle(0, 0, 44, 43));
	}
}
