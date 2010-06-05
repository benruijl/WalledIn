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
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class SpatialBehavior extends Behavior {
    protected Vector2f position;
    protected Vector2f velocity;
    protected Rectangle boundingBox;
    protected Circle boundingCircle;

    public SpatialBehavior(final Entity owner) {
        super(owner);
        position = new Vector2f();
        velocity = new Vector2f();
        boundingBox = new Rectangle();
        boundingCircle = new Circle();

        setAttribute(Attribute.POSITION, position); // create attribute
        setAttribute(Attribute.VELOCITY, velocity);
        setAttribute(Attribute.BOUNDING_RECT, boundingBox);
        setAttribute(Attribute.BOUNDING_CIRCLE, boundingCircle);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case POSITION:
                position = (Vector2f) getAttribute(attribute);
                break;
            case VELOCITY:
                velocity = (Vector2f) getAttribute(attribute);
                break;
            case BOUNDING_RECT:
                boundingBox = (Rectangle) getAttribute(attribute);
                // recreate circle
                boundingCircle = Circle.fromRect(boundingBox);
                setAttribute(Attribute.BOUNDING_CIRCLE, boundingCircle);
                break;
            }
        }
    }

    @Override
    public void onUpdate(final double delta) {
        /*
         * Vector2f scaledVelocity = new Vector2f(velocity); scaledVelocity =
         * scaledVelocity.scale((float) delta); position =
         * position.add(scaledVelocity); setAttribute(Attribute.POSITION,
         * position);
         */
    }
}
