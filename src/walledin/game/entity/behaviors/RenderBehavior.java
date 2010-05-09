package walledin.game.entity.behaviors;

import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;


public class RenderBehavior extends Behavior {
	

	public RenderBehavior(Entity owner, ZValues z) {
		super(owner);
		
		setAttribute(Attribute.Z_INDEX, z.z);
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub
		
	}

}
