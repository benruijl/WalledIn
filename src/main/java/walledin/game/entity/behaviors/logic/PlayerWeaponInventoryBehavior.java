/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.game.entity.behaviors.logic;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class PlayerWeaponInventoryBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger
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
        /* On death, drop the active weapon and remove the rest. */
        if (messageType == MessageType.DEATH) {
            getOwner().sendMessage(MessageType.DROP, Attribute.ACTIVE_WEAPON);
            setAttribute(Attribute.ACTIVE_WEAPON, null);
            weapons.clear();
            return;
        }

        if (messageType == MessageType.COLLIDED) {
            final Entity weapon = (Entity) data;
           
            if (weapon.getFamily().getParent() == Family.WEAPON) {
                LOG.info("Weapon collided: " + weapon.getName());
                
                if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)
                        || getOwner().getAttribute(Attribute.ACTIVE_WEAPON) != weapon) {

                    // is weapon already owned?
                    final Boolean hasWeapon = (Boolean) weapon
                            .getAttribute(Attribute.PICKED_UP);
                    if (hasWeapon != null && hasWeapon) {
                        return;
                    }

                    if (weapons.containsKey(weapon.getFamily())) {
                        return;
                    }

                    /* Tell the item that it will be picked up. */
                    weapon.sendMessage(MessageType.PICK_UP, getOwner());

                    weapons.put(weapon.getFamily(), weapon);
                    LOG.info("Adding weapon of family "
                            + weapon.getFamily().toString());

                    if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
                        setAttribute(Attribute.ACTIVE_WEAPON, weapon);

                        // set some attributes for the weapon
                        weapon.setAttribute(Attribute.ORIENTATION_ANGLE,
                                getAttribute(Attribute.ORIENTATION_ANGLE));
                    } else {
                        // remove weapon if picked up
                        weapon.remove();
                    }

                }
            }
        }

        if (messageType == MessageType.SELECT_WEAPON) {
            final Entity weapon = weapons.get(weaponKeyMap.get(data));
            
            if (weapon != null
                    && !getAttribute(Attribute.ACTIVE_WEAPON).equals(weapon)) {
                getEntityManager().add(weapon);

                final Entity oldWeapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);

                if (oldWeapon != null) {
                    oldWeapon.remove();
                }

                setAttribute(Attribute.ACTIVE_WEAPON, weapon);

                // set some attributes for the weapon
                weapon.setAttribute(Attribute.ORIENTATION_ANGLE,
                        getAttribute(Attribute.ORIENTATION_ANGLE));
            }
        }
    }

    @Override
    public void onUpdate(final double delta) {
        // TODO Auto-generated method stub

    }

}
