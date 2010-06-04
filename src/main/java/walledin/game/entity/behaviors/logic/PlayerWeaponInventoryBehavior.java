package walledin.game.entity.behaviors.logic;

import org.apache.log4j.Logger;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class PlayerWeaponInventoryBehavior extends Behavior {
	private final static Logger LOG = Logger.getLogger(PlayerWeaponInventoryBehavior.class);
	
	public PlayerWeaponInventoryBehavior(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.COLLIDED)
		{
			CollisionData colData = (CollisionData) data;

			if (colData.getCollisionEntity().getFamily().getParent() == Family.WEAPON) {
				if (getOwner().getAttribute(Attribute.WEAPON) != colData.getCollisionEntity())
				{
					LOG.info("Adding weapon");
				}
			}
		}
	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
