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

import walledin.engine.math.Circle;
import walledin.engine.math.Geometry;
import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.util.SettingsManager;

/**
 * This is handles the collision response for objects that are immovable. Other
 * objects colliding with it will not fly through, but will be stopped.
 * 
 * @author Ben Ruijl
 * 
 */
public class StaticObjectCollisionBehavior extends Behavior {
    /** The maximum depth of a collision resolving search. */
    private final int maxCollisionSearchDepth;

    /**
     * Creates a new StaticObjectCollisionResponse behavior.
     * 
     * @param owner
     *            Owner of this behavior
     */
    public StaticObjectCollisionBehavior(final Entity owner) {
        super(owner);

        maxCollisionSearchDepth = SettingsManager.getInstance().getInteger(
                "game.maxCollisionSearchDepth");
    }

    /**
     * Does a binary search to resolve a collision between a static and a moving
     * object.
     * 
     * @param maxDepth
     *            The maximum search depth
     * @param left
     *            Left bound
     * @param right
     *            Right bound
     * @param boundsA
     *            Bounding geometry of static object
     * @param boundsB
     *            Bounding geometry of moving object
     * @return The optimal position at which no collision occurs, given the
     *         maximum search depth.
     */
    final Vector2f doBinarySearch(final int maxDepth, Vector2f left,
            Vector2f right, final Geometry boundsA, final Geometry boundsB) {
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

        return left;
    }

    /**
     * Responds to a collision.
     * 
     * @param data
     *            Collision data
     */
    final void doResponse(final CollisionData data) {

        if (data.getCollisionEntity().getFamily() == Family.MAP) {
            return;
        }

        // FIXME: this is a hack, this has to be resolved in some other way
        if (data.getCollisionEntity().getFamily() == Family.FOAMGUN_BULLET) {
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

        /* Do a binary search in each direction */
        final Vector2f resolvedPosX = doBinarySearch(maxCollisionSearchDepth,
                oldPosB, oldPosB.getYVector().add(endPosB.getXVector()),
                boundsA, boundsB);
        Vector2f resolvedPos = doBinarySearch(maxCollisionSearchDepth,
                resolvedPosX,
                resolvedPosX.getXVector().add(endPosB.getYVector()), boundsA,
                boundsB);

        /*
         * Check if the old position is colliding. This is the case if the
         * resolved position is colliding, because then they are the same. If it
         * is, check if the new collision depth is smaller than the old one. If
         * so, allow the movement. This will prevent objects from getting stuck.
         */
        if (boundsA.intersects(boundsB.translate(oldPosB))) {
            final Circle circA = boundsA.asCircumscribedCircle();
            final Circle newCircB = boundsB.asCircumscribedCircle().translate(
                    endPosB);
            final Circle oldCircB = boundsB.asCircumscribedCircle().translate(
                    oldPosB);

            /*
             * Only allow the movement if it doesn't move over the center of the
             * object, else you will get tunneling.
             */
            if (newCircB.getPos().sub(oldCircB.getPos()).lengthSquared() < circA
                    .getPos().sub(oldCircB.getPos()).lengthSquared()) {

                final float newColDepth = boundsA.asCircumscribedCircle()
                        .intersectionDepth(
                                boundsB.asCircumscribedCircle().translate(
                                        endPosB));
                final float oldColDepth = boundsA.asCircumscribedCircle()
                        .intersectionDepth(
                                boundsB.asCircumscribedCircle().translate(
                                        oldPosB));

                if (newColDepth < oldColDepth) {
                    resolvedPos = endPosB;
                }
            }
        }

        data.getCollisionEntity().setAttribute(Attribute.POSITION, resolvedPos);

        /* Notify the object of this collision response */
        data.getCollisionEntity().sendMessage(
                MessageType.COLLIDED,
                new CollisionData(resolvedPos, oldPosB, endPosB, data
                        .getDelta(), getOwner()));

        // apply some sort of normal force?
        /*
         * data.getCollisionEntity().sendMessage(MessageType.APPLY_FORCE,
         * resolvedPos.sub(endPosB).scale(20000.0f));
         */
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            doResponse((CollisionData) data);
        }
    }

    @Override
    public void onUpdate(final double delta) {
        // TODO Auto-generated method stub

    }

}
