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

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class WeaponBehavior extends Behavior {
    private Entity owner; // entity that carries the gun
    private final int fireLag;
    private final Family bulletFamily;
    private boolean canShoot;
    private int lastShot; // frame of last shot

    public WeaponBehavior(final Entity owner, final int fireLag,
            final Family bulletFamily) {
        super(owner);
        this.fireLag = fireLag;
        lastShot = fireLag;
        canShoot = true;
        this.bulletFamily = bulletFamily;

        // can be picked up, is not owned by any player
        setAttribute(Attribute.COLLECTABLE, Boolean.TRUE);
        setAttribute(Attribute.ORIENTATION, Integer.valueOf(1));
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if ((Boolean) getAttribute(Attribute.COLLECTABLE)
                && messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;
            final Entity ent = colData.getCollisionEntity();

            if (!ent.getFamily().equals(Family.PLAYER)) {
                return;
            }

            owner = ent;

            /* Check if player had a previous weapon, if so drop it */
            /*
             * if (owner.hasAttribute(Attribute.ACTIVE_WEAPON)) {
             * owner.sendMessage(MessageType.DROP, Attribute.ACTIVE_WEAPON); }
             * 
             * owner.setAttribute(Attribute.ACTIVE_WEAPON, getOwner());
             */
            setAttribute(Attribute.COLLECTABLE, Boolean.FALSE);
        }

        if (messageType == MessageType.DROP) // to be called by Player only
        {
            setAttribute(Attribute.COLLECTABLE, Boolean.TRUE);
            owner = null;
        }

        if (messageType == MessageType.SHOOT) {
            if (canShoot) {
                final Entity player = (Entity) data;

                final int or = (Integer) player
                        .getAttribute(Attribute.ORIENTATION);
                final Vector2f playerPos = (Vector2f) player
                        .getAttribute(Attribute.POSITION);

                final Vector2f bulletPosition;

                // slightly more complicated, since the player pos is defined as
                // the top left
                if (or > 0) {
                    bulletPosition = playerPos.add(new Vector2f(50.0f, 20.0f));
                } else {
                    bulletPosition = playerPos.add(new Vector2f(-30.0f, 20.0f));
                }

                final Vector2f bulletAcceleration = new Vector2f(or * 30000.0f,
                        0);

                final EntityManager manager = getEntityManager();
                final Entity bullet = manager.create(bulletFamily, manager
                        .generateUniqueName(bulletFamily));

                bullet.setAttribute(Attribute.POSITION, bulletPosition);
                bullet.sendMessage(MessageType.APPLY_FORCE, bulletAcceleration);

                canShoot = false;
                lastShot = fireLag;
            }
        }
    }

    @Override
    public final void onUpdate(final double delta) {
        if (!canShoot) {
            lastShot--;

            if (lastShot <= 0) {
                canShoot = true;
            }
        }

    }

}
