package walledin.game;

import org.w3c.dom.Element;

import walledin.game.entity.MessageType;

public class HealthKitItem extends Item {

	public HealthKitItem(String name, String texPart, Element el) {
		super(name, texPart);
		
	}
	
	@Override
	public void sendMessage(MessageType messageType, Object data) {
		// TODO Auto-generated method stub
		super.sendMessage(messageType, data);
	}
	
	@Override
	public void sendUpdate(double delta) {
		// TODO Auto-generated method stub
		super.sendUpdate(delta);
	}

}