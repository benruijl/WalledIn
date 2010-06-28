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

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PhysicsBehavior extends Behavior {
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);

    private final float mass; // mass of object
    private final Vector2f gravity; // acceleration of gravity
    private final float frictionCoefficient; // part of the velocity that is
    // kept
    private Vector2f position;
    private Vector2f velocity;
    private Vector2f acceleration = new Vector2f(0, 0);

    public PhysicsBehavior(final Entity owner, final float mass) {
        this(owner, mass, true, true);
    }

    public PhysicsBehavior(final Entity owner, final float mass,
            final boolean doGravity, final boolean doFriction) {
        super(owner);

        if (mass == 0) {
            LOG
                    .warn("Mass of "
                            + getOwner().getName()
                            + " is 0. Applying a force will give an infinite acceleration."); 
        }

        this.mass = mass;
        setAttribute(Attribute.MASS, mass);
        setAttribute(Attribute.POSITION, new Vector2f());
        setAttribute(Attribute.VELOCITY, new Vector2f());
        position = (Vector2f) getAttribute(Attribute.POSITION);
        velocity = (Vector2f) getAttribute(Attribute.VELOCITY);

        if (doGravity) {
            gravity = new Vector2f(0, 300.0f);
        } else {
            gravity = new Vector2f(0, 0);
        }
        if (doFriction) {
            frictionCoefficient = 0.02f;
        } else {
            frictionCoefficient = 0;
        }
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.APPLY_FORCE) {
            acceleration = acceleration.add(((Vector2f) data).scale(1 / mass));
        } else if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case POSITION:
                position = (Vector2f) getAttribute(attribute);
                break;
            case VELOCITY:
                velocity = (Vector2f) getAttribute(attribute);
                break;
            }
        }
    }

    @Override
    public void onUpdate(final double delta) {
        acceleration = acceleration.add(gravity);

        // add friction
        acceleration = acceleration.add(new Vector2f(-Math.signum(velocity
                .getX())
                * velocity.getX() * velocity.getX() * frictionCoefficient,
                -Math.signum(velocity.getY()) * velocity.getY()
                        * velocity.getY() * frictionCoefficient));

        final Vector2f velNew = velocity.add(acceleration.scale((float) delta));
        final Vector2f posNew = position.add(velNew.scale((float) delta));

        setAttribute(Attribute.VELOCITY, velNew);
        setAttribute(Attribute.POSITION, posNew);

        acceleration = new Vector2f(0, 0);
    }

}
