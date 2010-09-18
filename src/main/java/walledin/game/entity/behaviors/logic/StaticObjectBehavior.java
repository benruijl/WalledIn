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

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.collision.StaticObject;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

/**
 * This class handles the properties of static objects. Static objects implement
 * the StaticObject interface and thus they can be added to a quadtree.
 * 
 * @author Ben Ruijl
 * 
 */
public class StaticObjectBehavior extends AbstractBehavior implements
        StaticObject {

    public StaticObjectBehavior(Entity owner) {
        super(owner);
    }

    @Override
    public Rectangle getBoudingRectangle() {
        return ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                .asRectangle().translate(
                        (Vector2f) getAttribute(Attribute.POSITION));
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
    }

    @Override
    public void onUpdate(double delta) {
    }

}
