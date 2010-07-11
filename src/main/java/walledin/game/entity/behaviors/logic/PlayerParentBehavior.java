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

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerParentBehavior extends Behavior {

    public PlayerParentBehavior(final Entity owner) {
        super(owner);
    }

    /**
     * This function updates the children of the player (think of weapons) with
     * the current player position, orientation etc.
     */
    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if (messageType != MessageType.ATTRIBUTE_SET) {
            return;
        }

        if (!getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
            return;
        }

        final Entity weapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);
        final Attribute attrib = (Attribute) data;
        final float or = (Float) getAttribute(Attribute.ORIENTATION_ANGLE);
        final Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
        final Vector2f vel = (Vector2f) getAttribute(Attribute.VELOCITY);
        final Vector2f cursorPos = (Vector2f) getAttribute(Attribute.CURSOR_POS);

        switch (attrib) {
        case CURSOR_POS:
            weapon.setAttribute(Attribute.CURSOR_POS, cursorPos);
            break;
        case POSITION:
            weapon.setAttribute(Attribute.POSITION, pos.add(new Vector2f(35.0f,
                    20.0f)));
            break;
        case VELOCITY:
            weapon.setAttribute(Attribute.VELOCITY, vel);
            break;
        case ORIENTATION_ANGLE:
            weapon.setAttribute(Attribute.ORIENTATION_ANGLE, or);
            break;
        default:
            break;
        }

    }

    @Override
    public void onUpdate(final double delta) {
    }
}
