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
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
    /** Standard acceleration by gravity. */
    private static final float GRAVITY_ACCELERATION_CONSTANT = 300.0f;
    /** Standard friction coefficient. */
    private static final float FRICTION_COEFFICIENT_CONSTANT = 0.02f;
    /** Mass of object. */
    private final float mass;
    /** Gravity acceleration. */
    private final Vector2f gravity;
    /**
     * The friction coefficient. In WalledIn this is defined as the part of the
     * velocity squared that counters the movement: a = -v^2 * c.
     */
    private final float frictionCoefficient;
    /** The current position. */
    private Vector2f position;
    /** The current velocity. */
    private Vector2f velocity;
    /** The current acceleration. */
    private Vector2f acceleration;

    /**
     * Creates a new standard physics behavior that gives the object gravity and
     * friction.
     * 
     * @param owner
     *            Owner entity
     * @param mass
     *            Mass of the object
     */
    public PhysicsBehavior(final Entity owner, final float mass) {
        this(owner, mass, true, true);
    }

    /**
     * Creates a new physics behavior with special settings.
     * 
     * @param owner
     *            Owner entity
     * @param mass
     *            Mass
     * @param doGravity
     *            Set to true if gravity should be applied
     * @param doFriction
     *            Set to true if friction should be applied
     */
    public PhysicsBehavior(final Entity owner, final float mass,
            final boolean doGravity, final boolean doFriction) {
        super(owner);
        acceleration = new Vector2f();

        if (mass == 0) {
            LOG.warn("Mass of " + getOwner().getName()
                    + " is 0. Applying a force will "
                    + "give an infinite acceleration.");
        }

        this.mass = mass;
        setAttribute(Attribute.MASS, mass);
        
        if (!getOwner().hasAttribute(Attribute.POSITION)) {
            setAttribute(Attribute.POSITION, new Vector2f());
        }
        
        if (!getOwner().hasAttribute(Attribute.VELOCITY)) {
            setAttribute(Attribute.VELOCITY, new Vector2f());
        }
        
        position = (Vector2f) getAttribute(Attribute.POSITION);
        velocity = (Vector2f) getAttribute(Attribute.VELOCITY);

        if (doGravity) {
            gravity = new Vector2f(0, GRAVITY_ACCELERATION_CONSTANT);
        } else {
            gravity = new Vector2f(0, 0);
        }

        if (doFriction) {
            frictionCoefficient = FRICTION_COEFFICIENT_CONSTANT;
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
            default:
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
                * velocity.getX()
                * velocity.getX()
                * frictionCoefficient, -Math.signum(velocity.getY())
                * velocity.getY() * velocity.getY() * frictionCoefficient));

        final Vector2f velNew = velocity.add(acceleration.scale((float) delta));
        final Vector2f posNew = position.add(velNew.scale((float) delta));

        setAttribute(Attribute.VELOCITY, velNew);
        setAttribute(Attribute.POSITION, posNew);

        acceleration = new Vector2f(0, 0);
    }

}
