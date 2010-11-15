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
package walledin.game;

import walledin.game.entity.Entity;
import walledin.game.entity.Family;

import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;

public class GameCollisionDispatcher extends CollisionDispatcher {
    private final EntityManager entityManager;

    public GameCollisionDispatcher(EntityManager entityManager) {
        super(new DefaultCollisionConfiguration());

        this.entityManager = entityManager;
    }

    @Override
    public boolean needsResponse(CollisionObject body0, CollisionObject body1) {
        Entity ent1 = entityManager.get((String) body0.getUserPointer());
        Entity ent2 = entityManager.get((String) body1.getUserPointer());

        if (ent1 == null || ent2 == null) {
            return false;
        }

        if (ent1.getFamily() == Family.PLAYER
                && ent2.getFamily().getParent() == Family.WEAPON
                || ent2.getFamily() == Family.PLAYER
                && ent1.getFamily().getParent() == Family.WEAPON) {
            return false;
        }

        return super.needsResponse(body0, body1);
    }

}
