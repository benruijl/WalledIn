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
import walledin.game.entity.MessageType;

public class StandardCollisionResponseBehavior extends Behavior {

    public StandardCollisionResponseBehavior(Entity owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    /**
     * Does collision response.
     * 
     * @param data
     *            Collision data
     */
    private void doRepsonse(final CollisionData data) {
        // TODO: check if response is already done

        if (data.getNewPos() == data.getOldPos()) {
            return;
        }

        // transform to a system in which object A stands still
        final Vector2f velA = data.getTheorPos().sub(data.getOldPos());
        final Vector2f velB = (Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.VELOCITY);

        final Vector2f oldPosB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION)).sub(velB);

        final Vector2f vel = velB.sub(velA);

        float dx, dy; // position

        // do very basic col. response
        Geometry boundsA = (Geometry) getOwner().getAttribute(
                Attribute.BOUNDING_GEOMETRY);
        Geometry boundsB = (Geometry) data.getCollisionEntity().getAttribute(
                Attribute.BOUNDING_GEOMETRY);

        if (boundsB.translate(
                new Vector2f(oldPosB.getX() + vel.getX(), oldPosB.getY()))
                .intersects(boundsA)) {
            dx = 0;
        } else {
            dx = vel.getX();
        }

        if (boundsB.translate(
                new Vector2f(oldPosB.getX() + dx, oldPosB.getY() + vel.getY()))
                .intersects(boundsA)) {
            dy = 0;
        } else {
            dy = vel.getY();
        }

        setAttribute(Attribute.POSITION, data.getOldPos().sub(
                new Vector2f(dx, dy)));
        // data.getCollisionEntity().setAttribute(Attribute.POSITION,
        // oldPosB.
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        if (messageType == MessageType.COLLIDED) {
            doRepsonse((CollisionData) data);
        }

    }

    @Override
    public void onUpdate(double delta) {
        // TODO Auto-generated method stub

    }

}
