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
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class BulletBehavior extends Behavior {
    /** The damage the player takes from this bullet. */
    private final int damage;

    public BulletBehavior(final Entity owner, final int damage) {
        super(owner);
        this.damage = damage;
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;

            // if collided with map, destroy
            if (colData.getCollisionEntity().getFamily().equals(Family.MAP)) {
                getOwner().remove();
            }

            // if collided with entity that has a health component, remove and
            // do damage
            if (colData.getCollisionEntity().hasAttribute(Attribute.HEALTH)) {
                colData.getCollisionEntity().sendMessage(
                        MessageType.TAKE_DAMAGE, Integer.valueOf(damage));

                getOwner().remove();
            }
        }

        if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attrib = (Attribute) data;

            /* If the velocity is changed, change the angle of the bullet */
            if (attrib == Attribute.VELOCITY) {
                final Vector2f velocity = (Vector2f) getAttribute(Attribute.VELOCITY);
                setAttribute(Attribute.ORIENTATION_ANGLE,
                        (float) Math.atan2(velocity.getY(), velocity.getX()));
            }
        }

    }

    @Override
    public void onUpdate(final double delta) {
    }
}
