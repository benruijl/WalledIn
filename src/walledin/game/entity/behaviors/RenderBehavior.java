package walledin.game.entity.behaviors;

import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class RenderBehavior extends Behavior {

	public RenderBehavior(final Entity owner, final ZValues z) {
		super(owner);

		setAttribute(Attribute.Z_INDEX, z.z);
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdate(final double delta) {
		// TODO Auto-generated method stub

	}

}
