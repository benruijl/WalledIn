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

import org.apache.log4j.Logger;

import walledin.engine.math.Geometry;
import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class StandardCollisionResponseBehavior extends Behavior {
    private static final Logger LOG = Logger
            .getLogger(StandardCollisionResponseBehavior.class);

    public StandardCollisionResponseBehavior(final Entity owner) {
        super(owner);
        // TODO Auto-generated constructor stub
    }

    /**
     * Does collision response.
     * 
     * @param data
     *            Collision data
     */
    private void doResponse(final CollisionData data) {
        // TODO: check if response is already done

        // check if object is moving
        if (data.getNewPos() == data.getOldPos()) {
            return;
        }

        if (data.getCollisionEntity().getFamily() == Family.MAP) {
            return; // do not check against map
        }

        if (data.getCollisionEntity().getFamily().getParent() == Family.WEAPON) {
            return; // FIXME: nasty hack
        }

        // transform to a system in which object A stands still
        final Vector2f velA = data.getTheorPos().sub(data.getOldPos());
        final Vector2f velB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.VELOCITY)).scale((float) data
                .getDelta());

        final Vector2f oldPosB = ((Vector2f) data.getCollisionEntity()
                .getAttribute(Attribute.POSITION)).sub(velB);

        final Vector2f vel = velB.sub(velA); // velocity of particle B in new
                                             // system

        final float dx, dy; // position

        // do very basic collision response
        final Geometry boundsA = (Geometry) getOwner().getAttribute(
                Attribute.BOUNDING_GEOMETRY);
        final Geometry boundsB = (Geometry) data.getCollisionEntity()
                .getAttribute(Attribute.BOUNDING_GEOMETRY);

        /*
         * if (boundsB.translate( new Vector2f(oldPosB.getX() + vel.getX(),
         * oldPosB.getY())) .intersects(boundsA))
         */

        /*
         * if (boundsB.translate( new Vector2f(oldPosB.getX() + vel.getX(),
         * oldPosB.getY())) .intersects(boundsA)) { dx = 0; } else { dx =
         * vel.getX(); }
         * 
         * if (boundsB.translate( new Vector2f(oldPosB.getX() + dx,
         * oldPosB.getY() + vel.getY())) .intersects(boundsA)) { dy = 0; } else
         * { dy = vel.getY(); }
         */

        // setAttribute(Attribute.POSITION, data.getOldPos().sub(
        // new Vector2f(dx, dy)));

        // LOG.info("I was here: " + dx + " " + dy);

        final Float massA = (Float) getAttribute(Attribute.MASS);
        final Float massB = (Float) data.getCollisionEntity().getAttribute(
                Attribute.MASS);

        if (massA == null || massB == null) {
            return;
        }

        final Vector2f finalVelA = velA
                .scale((massA - massB) / (massA + massB)).add(
                        velB.scale(2 * massB / (massA + massB)));
        final Vector2f finalVelB = velB
                .scale((massB - massA) / (massA + massB)).add(
                        velA.scale(2 * massA / (massA + massB)));

        setAttribute(Attribute.VELOCITY,
                finalVelA.scale((float) (1 / data.getDelta())));
        setAttribute(Attribute.POSITION,
                ((Vector2f) getAttribute(Attribute.POSITION)).add(finalVelA));
        data.getCollisionEntity().setAttribute(Attribute.VELOCITY,
                finalVelB.scale((float) (1 / data.getDelta())));
        data.getCollisionEntity().setAttribute(
                Attribute.POSITION,
                ((Vector2f) data.getCollisionEntity().getAttribute(
                        Attribute.POSITION)).add(finalVelB));

        // getOwner().sendMessage(MessageType.APPLY_FORCE, new Vector2f(dx *
        // 100, dy * 100).scale((float) (1 / data.getDelta())));

        // setAttribute(Attribute.POSITION, data.getOldPos().add(new
        // Vector2f(dx, dy)));
        // setAttribute(Attribute.VELOCITY, new Vector2f(dx, dy).scale((float)
        // (1 / data.getDelta())));

        // setAttribute(Attribute.VELOCITY, velA.sub(new Vector2f(dx,
        // dy)).scale((float) (1/ data.getDelta())));

        // data.getCollisionEntity().setAttribute(Attribute.POSITION,
        // oldPosB.
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
