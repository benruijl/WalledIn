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

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerAnimationBehavior extends AnimationBehavior {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlayerAnimationBehavior.class);

    private float walkAnimFrame;
    private final float animSpeed = 0.002f;
    private Vector2f velocity;

    public PlayerAnimationBehavior(final Entity owner) {
        super(owner);
        setAttribute(Attribute.WALK_ANIM_FRAME, new Float(0));
        velocity = new Vector2f();
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
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
        if (Math.abs(velocity.getX()) > 0.5f) {
            walkAnimFrame += animSpeed * velocity.getX();
            walkAnimFrame %= 2 * Math.PI;
            setAttribute(Attribute.WALK_ANIM_FRAME, new Float(walkAnimFrame));
        }
    }
}
