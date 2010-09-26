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
import walledin.game.collision.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

/**
 * This foam bullet will stick to the cursor position.
 * 
 * @author Ben Ruijl
 * 
 */
public class StickyFoamBulletBehavior extends BulletBehavior {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(StickyFoamBulletBehavior.class);
    private boolean blownUp;
    private final float allowedRadius = 100;

    public StickyFoamBulletBehavior(final Entity owner) {
        super(owner, 0);
        blownUp = false;
    }

    public final void spawnBullet() {
        final EntityManager manager = getEntityManager();
        final Entity particle = manager.create(Family.FOAM_PARTICLE);

        particle.setAttribute(Attribute.POSITION,
                getAttribute(Attribute.POSITION));
        particle.setAttribute(Attribute.ORIENTATION_ANGLE,
                getAttribute(Attribute.ORIENTATION_ANGLE));
        particle.setAttribute(Attribute.OWNED_BY,
                getAttribute(Attribute.OWNED_BY));

        blownUp = true;
        getOwner().remove();
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        super.onMessage(messageType, data);

        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;
            if (!blownUp) {

                if (colData.getCollisionEntity().getFamily() == Family.MAP
                /*
                 * || colData.getCollisionEntity().getFamily()
                 * .equals(Family.FOAM_PARTICLE)
                 */) {
                    spawnBullet();
                }
            }
        }

    }

    @Override
    public final void onUpdate(final double delta) {
        if (!blownUp) {
            if (((Vector2f) getAttribute(Attribute.POSITION)).sub(
                    (Vector2f) getAttribute(Attribute.TARGET)).lengthSquared() < allowedRadius) {
                spawnBullet();
            }
        }

        super.onUpdate(delta);
    }
}
