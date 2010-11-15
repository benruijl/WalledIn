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

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;

import walledin.engine.physics.ContactListener;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class ContactHandler implements ContactListener {
    private final EntityManager entityManager;

    public ContactHandler(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    @Override
    public void processContact(ManifoldPoint point,
            PersistentManifold contactManifold) {
        CollisionObject a = (CollisionObject) contactManifold.getBody0();
        CollisionObject b = (CollisionObject) contactManifold.getBody1();

        Entity ent1 = entityManager.get((String) a.getUserPointer());

        Entity ent2 = entityManager.get((String) b.getUserPointer());

        if (ent1 != null && ent2 != null) {
            ent1.sendMessage(MessageType.COLLIDED, ent2);
            ent2.sendMessage(MessageType.COLLIDED, ent1);
        }

    }
}
