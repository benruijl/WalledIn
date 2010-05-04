package walledin.game.entity.behaviors;

import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.MessageType;

public class SpatialBehavior extends Behavior {
	Vector2f pos;
	Vector2f velocity;

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.MOVE)
		{
			pos = getAttribute(Attribute.POSITION);
			pos.add((Vector2f)data);
			setAttribute(Attribute.POSITION, pos);
		}
		
		if (messageType == MessageType.SETPOS)
		{
			setAttribute(Attribute.POSITION, (Vector2f)data);
		}

	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
