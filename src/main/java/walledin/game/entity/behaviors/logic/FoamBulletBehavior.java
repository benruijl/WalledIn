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

import walledin.game.CollisionManager.CollisionData;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class FoamBulletBehavior extends BulletBehavior {
    private boolean blownUp;

    public FoamBulletBehavior(final Entity owner) {
        super(owner, 0);
        blownUp = false;
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        super.onMessage(messageType, data);
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;
            if (!blownUp) {

                // if collided with map or other foam particle create a
                // foam particle
                if (colData.getCollisionEntity().getFamily() == Family.MAP
                        || colData.getCollisionEntity().getFamily()
                                .equals(Family.FOAM_PARTICLE)) {
                    final EntityManager manager = getEntityManager();
                    final Entity particle = manager.create(
                            Family.FOAM_PARTICLE,
                            manager.generateUniqueName(Family.FOAM_PARTICLE));

                    particle.setAttribute(Attribute.POSITION,
                            getAttribute(Attribute.POSITION));
                    blownUp = true;

                    getOwner().remove(); // remove the foam bullet
                }
            }
        }
    }
}
