package walledin.game.entity.behaviors.logic;

import java.util.HashMap;
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
    private final Map<Family, Entity> weapons;
    private final Map<Integer, Family> weaponKeyMap;

    public PlayerWeaponInventoryBehavior(final Entity owner) {
        super(owner);

        weapons = new HashMap<Family, Entity>();
        weaponKeyMap = new HashMap<Integer, Family>();

        /* Add default guns to list */
        weaponKeyMap.put(1, Family.HANDGUN);
        weaponKeyMap.put(2, Family.FOAMGUN);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;
            final Entity weapon = colData.getCollisionEntity();

            if (weapon.getFamily().getParent() == Family.WEAPON) {
                if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)
                        || getOwner().getAttribute(Attribute.ACTIVE_WEAPON) != weapon) {
                    if (weapons.containsKey(weapon.getFamily())) {
                        return;
                    }

                    weapons.put(weapon.getFamily(), weapon);
                    LOG.info("Adding weapon of family "
                            + weapon.getFamily().toString());

                    if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
                        getOwner()
                                .setAttribute(Attribute.ACTIVE_WEAPON, weapon);
                    } else {
                        weapon.remove();
                    }

                }
            }
        }

        if (messageType == MessageType.SELECT_WEAPON) {
            final Entity weapon = weapons.get(weaponKeyMap.get(data));

            if (weapon != null && !getAttribute(Attribute.ACTIVE_WEAPON).equals(weapon)) {
                getEntityManager().add(weapon);
                
                Entity oldWeapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);
                oldWeapon.remove();
                
                setAttribute(Attribute.ACTIVE_WEAPON, weapon);
            }
        }
    }

    @Override
    public void onUpdate(final double delta) {
        // TODO Auto-generated method stub

    }

}
