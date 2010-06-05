package walledin.game.entity.behaviors.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class PlayerWeaponInventoryBehavior extends Behavior {
	private final static Logger LOG = Logger
			.getLogger(PlayerWeaponInventoryBehavior.class);
	private Map<Family, Entity> weapons;
	private Map<Integer, Family> weaponKeyMap;

	public PlayerWeaponInventoryBehavior(Entity owner) {
		super(owner);

		weapons = new HashMap<Family, Entity>();
		weaponKeyMap = new HashMap<Integer, Family>();

		/* Add default guns to list */
		weaponKeyMap.put(1, Family.HANDGUN);
		weaponKeyMap.put(2, Family.FOAMGUN);
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.COLLIDED) {
			CollisionData colData = (CollisionData) data;
			Entity weapon = colData.getCollisionEntity();

			if (weapon.getFamily().getParent() == Family.WEAPON) {
				if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)
						|| getOwner().getAttribute(Attribute.ACTIVE_WEAPON) != weapon) {
					if (weapons.containsKey(weapon.getFamily()))
						return;

					weapons.put(weapon.getFamily(), weapon);
					LOG.info("Adding weapon of family "
							+ weapon.getFamily().toString());
					
					if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON))
						getOwner().setAttribute(Attribute.ACTIVE_WEAPON, weapon);
				}
			}
		}

		if (messageType == MessageType.SELECT_WEAPON) {
			Entity weapon = weapons.get(weaponKeyMap.get((Integer) data));

			if (weapon != null)
				setAttribute(Attribute.ACTIVE_WEAPON, weapon);
		}
	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
