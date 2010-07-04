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
package walledin.game.entity.behaviors.physics;

import walledin.engine.math.Geometry;
import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

/**
 * This is handles the collision response for objects that are immovable. Other
 * objects colliding with it will not fly through, but will be stopped.
 * 
 * @author Ben Ruijl
 * 
 */
public class StaticObjectCollisionResponse extends Behavior {

    public StaticObjectCollisionResponse(final Entity owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    void doResponse(final CollisionData data) {

        if (data.getCollisionEntity().getFamily() == Family.MAP) {
            return;
        }

        final Vector2f velB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.VELOCITY)).scale((float) data
                .getDelta());

        final Vector2f endPosB = (Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION);
        final Vector2f oldPosB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION)).sub(velB);

        Geometry boundsA = (Geometry) getAttribute(Attribute.BOUNDING_GEOMETRY);
        final Geometry boundsB = (Geometry) data.getCollisionEntity()
                .getAttribute(Attribute.BOUNDING_GEOMETRY);

        boundsA = boundsA
                .translate((Vector2f) getAttribute(Attribute.POSITION));

        /* Do a binary search to resolve the collision */
        final int maxDepth = 4;
        Vector2f left = oldPosB;
        Vector2f right = endPosB;
        int depth = 0;
        while (depth < maxDepth) {
            final Vector2f mid = left.add(right.sub(left).scale(0.5f));

            if (boundsB.translate(mid).intersects(boundsA)) {
                right = mid;
            } else {
                left = mid;
            }
            depth++;
        }

        final Vector2f resolvedPos = left;// .add(right.sub(left).scale(0.5f));

        data.getCollisionEntity().setAttribute(Attribute.POSITION, resolvedPos);
        data.getCollisionEntity().setAttribute(Attribute.VELOCITY,
                new Vector2f(0, 0));
        // resolvedPos.sub(oldPosB).scale(1 / (float) data.getDelta()));

    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            doResponse((CollisionData) data);
        }
    }

    @Override
    public void onUpdate(final double delta) {
        // TODO Auto-generated method stub

    }

}
