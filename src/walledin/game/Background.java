package walledin.game;

import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.BackgroundRenderBehavior;
import walledin.game.entity.behaviors.PlayerRenderBehavior;

public class Background extends Entity {

	public Background(String name) {
		super(name);
		
		addBehavior(new BackgroundRenderBehavior(this));
	}

}
