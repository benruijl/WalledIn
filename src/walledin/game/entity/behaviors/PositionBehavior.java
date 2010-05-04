package walledin.game.entity.behaviors;

import walledin.engine.Vector2f;
import walledin.game.entity.Behavior;

public class PositionBehavior extends Behavior {
	Vector2f pos;
	
	public PositionBehavior() {
		pos = new Vector2f();
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(Vector2f pos) {
		this.pos = pos;
	}
}
