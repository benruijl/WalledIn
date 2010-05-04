package walledin.game.components;

import walledin.engine.Vector2f;

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
