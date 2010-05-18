package walledin.game;

import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.BackgroundRenderBehavior;

public class Background extends Entity {

	public Background(final String name) {
		super(name);

		addBehavior(new BackgroundRenderBehavior(this));
	}

}
