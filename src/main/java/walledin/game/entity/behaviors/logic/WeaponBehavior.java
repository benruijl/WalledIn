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

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.util.Utils;

public class WeaponBehavior extends Behavior {
    private static final Logger LOG = Logger.getLogger(WeaponBehavior.class);

    private final float bulletAccelerationConstant = 30000.0f;
    private final Vector2f bulletStartPositionRight = new Vector2f(50.0f, 20.0f);
    private final Vector2f bulletStartPositionLeft = new Vector2f(-30.0f, 20.0f);

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

        setAttribute(Attribute.PICKED_UP, Boolean.FALSE);
        setAttribute(Attribute.ORIENTATION_ANGLE, Float.valueOf(0));
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.DROP) { // to be called by Player only
            LOG.info("Weapon " + getOwner().getName() + " dropped.");
            setAttribute(Attribute.PICKED_UP, Boolean.FALSE);
            owner = null;
        }

        if (messageType == MessageType.SHOOT) {
            if (canShoot) {
                final Entity player = (Entity) data;

                final boolean facingRight = Utils.getCircleHalf((Float) player
                        .getAttribute(Attribute.ORIENTATION_ANGLE)) == 1;
                final Vector2f playerPos = (Vector2f) player
                        .getAttribute(Attribute.POSITION);

                final Vector2f bulletPosition;

                // slightly more complicated, since the player pos is defined as
                // the top left
                if (facingRight) {
                    bulletPosition = playerPos.add(bulletStartPositionRight);
                } else {
                    bulletPosition = playerPos.add(bulletStartPositionLeft);
                }

                final Vector2f target = (Vector2f) getAttribute(Attribute.CURSOR_POS);
                final Vector2f bulletAcceleration = target.sub(bulletPosition)
                        .normalize().scale(bulletAccelerationConstant);

                final EntityManager manager = getEntityManager();
                final Entity bullet = manager.create(bulletFamily,
                        manager.generateUniqueName(bulletFamily));

                bullet.setAttribute(Attribute.POSITION, bulletPosition);
                bullet.setAttribute(Attribute.TARGET, target);
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
