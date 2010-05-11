package walledin.game;

import walledin.engine.math.Rectangle;
import walledin.game.entity.MessageType;

public class HealthKitItem extends Item {

	public HealthKitItem(final String name, final String texPart, Rectangle destRect) {
		super(name, texPart, destRect);
		
	}
	
	@Override
	public void sendMessage(final MessageType messageType, final Object data) {
		// TODO Auto-generated method stub
		super.sendMessage(messageType, data);
	}
	
	@Override
	public void sendUpdate(final double delta) {
		// TODO Auto-generated method stub
		super.sendUpdate(delta);
	}

}
