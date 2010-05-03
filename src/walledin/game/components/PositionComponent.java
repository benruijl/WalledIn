package walledin.game.components;

import walledin.engine.Vector2f;

public class PositionComponent extends Component {
	Vector2f pos;
	
	public PositionComponent() {
		pos = new Vector2f();
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(Vector2f pos) {
		this.pos = pos;
	}

	@Override
	public void onMessage(int message) {
		// TODO Auto-generated method stub
		
	}
	
	

}
