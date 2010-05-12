package walledin.game;

import walledin.engine.math.Rectangle;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthKitItem extends Item {
	private int strength;

	public HealthKitItem(final String name, final String texPart, Rectangle destRect) {
		super(name, texPart, destRect);
		
		strength = 10; // default
	}
	
	@Override
	public void sendMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.COLLIDED)
		{
			// assumes colliding entity has a health component
			Entity ent = (Entity)data;
			ent.sendMessage(MessageType.RESTORE_HEALTH, Integer.valueOf(strength));
		}

		
		super.sendMessage(messageType, data);
	}
	
	@Override
	public void sendUpdate(final double delta) {
		// TODO Auto-generated method stub
		super.sendUpdate(delta);
	}

}
