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

import walledin.engine.math.Matrix2f;
import walledin.engine.math.Vector2f;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class GrenadeBehavior extends AbstractBehavior {
    /**
     * The number of particles created from the explosion. They will fly in
     * different directions.
     */
    private static final int NUMBER_OFPARTICALS = 5;
    /** Explode time in seconds. */
    private static final double EXPLODE_TIME = 2.0;
    private double time = 0;
    private final Vector2f particleTarget = new Vector2f(100.0f, 0);
    private final Vector2f particleAcc = new Vector2f(30000.0f, 0);

    public GrenadeBehavior(final Entity owner) {
        super(owner);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
    }

    @Override
    public void onUpdate(final double delta) {
        time += delta;

        /* TODO: use shoot message? */
        if (time > EXPLODE_TIME) {
            /* Explode! */

            for (int i = 0; i < NUMBER_OFPARTICALS; i++) {
                final Entity foamBullet = getOwner().getEntityManager().create(
                        Family.FOAMGUN_BULLET);
                foamBullet.setAttribute(Attribute.POSITION,
                        getAttribute(Attribute.POSITION));

                foamBullet.sendMessage(MessageType.APPLY_FORCE, new Matrix2f(-i
                        * Math.PI / (NUMBER_OFPARTICALS - 1))
                        .apply(particleAcc));

                foamBullet
                        .setAttribute(Attribute.TARGET, ((Vector2f) foamBullet
                                .getAttribute(Attribute.POSITION))
                                .add(new Matrix2f(-i * Math.PI
                                        / (NUMBER_OFPARTICALS - 1))
                                        .apply(particleTarget)));

                foamBullet.setAttribute(Attribute.OWNED_BY,
                        getAttribute(Attribute.OWNED_BY));
            }

            getOwner().remove();
        }

    }
}
