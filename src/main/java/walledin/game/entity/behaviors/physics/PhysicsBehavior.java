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
    private final Vector2f gravity; // acceleration of gravity
    private final float frictionCoefficient; // part of the velocity that is
    // kept
    private Vector2f position;
    private Vector2f velocity;
    private Vector2f acceleration = new Vector2f(0, 0);

    public PhysicsBehavior(final Entity owner) {
        this(owner, true, true);
    }

    public PhysicsBehavior(final Entity owner, final boolean doGravity,
            final boolean doFriction) {
        super(owner);
        position = new Vector2f();
        velocity = new Vector2f();
        setAttribute(Attribute.POSITION, position); // create attribute
        setAttribute(Attribute.VELOCITY, velocity);
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
            acceleration = acceleration.add((Vector2f) data);
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
        acceleration = acceleration.add(new Vector2f(-Math.signum(velocity.x)
                * velocity.x * velocity.x * frictionCoefficient, -Math
                .signum(velocity.y)
                * velocity.y
                * velocity.y
                * frictionCoefficient));

        final Vector2f velNew = velocity.add(acceleration.scale((float) delta));
        final Vector2f posNew = position.add(velNew.scale((float) delta));

        setAttribute(Attribute.VELOCITY, velNew);
        setAttribute(Attribute.POSITION, posNew);

        acceleration = new Vector2f(0, 0);
    }

}
