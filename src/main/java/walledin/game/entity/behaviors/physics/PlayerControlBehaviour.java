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

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends SpatialBehavior {
<<<<<<< HEAD
    private static final Logger LOG = Logger
            .getLogger(PlayerControlBehaviour.class);
=======
>>>>>>> master
    private static final float MOVE_SPEED = 240.0f;
    private static final float JUMP_SPEED = 8000.0f;
    private boolean canJump;
    private Set<Integer> keysDown;

    public PlayerControlBehaviour(final Entity owner) {
        super(owner);
        keysDown = new HashSet<Integer>();
        setAttribute(Attribute.KEYS_DOWN, keysDown);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;

<<<<<<< HEAD
            if (colData.getNewPos().getY() < colData.getTheorPos().getY())
                canJump = true;
=======
            if (colData.getNewPos().getY() < colData.getTheorPos().getY()) {
                canJump = true;
            }
>>>>>>> master
        } else if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case KEYS_DOWN:
                keysDown = (Set<Integer>) getAttribute(attribute);
                break;
            }
<<<<<<< HEAD
        } else if (messageType == MessageType.DROP) {
            if (data == null) // drop all
            {
                // FIXME: find better way to drop all entities
                onMessage(MessageType.DROP, Attribute.ACTIVE_WEAPON);
                return;
            }

            Attribute at = (Attribute) data;

            if (getOwner().hasAttribute(at)) {
                Entity ent = (Entity) getAttribute(at);
                ent.sendMessage(MessageType.DROP, null);
                setAttribute(at, null);
            } else
                LOG.warn("Trying to remove attribute " + at.toString()
                        + ", but entity " + getOwner().getName()
                        + " does not have this attribute.");
=======
        } else if (messageType == MessageType.DROP) { // drop all items
            if (getOwner().hasAttribute(Attribute.WEAPON)) {
                final Entity weapon = (Entity) getOwner().getAttribute(
                        Attribute.WEAPON);
                weapon.setAttribute(Attribute.COLLECTABLE, Boolean.TRUE);
                setAttribute(Attribute.WEAPON, null); // disown weapon
            }
>>>>>>> master
        }

        super.onMessage(messageType, data);
    }

    @Override
    public void onUpdate(final double delta) {
        float x = 0;
        float y = 0;

        if (keysDown.contains(KeyEvent.VK_RIGHT)
                || keysDown.contains(KeyEvent.VK_D)) {
            x += MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION, 1);

        }
        if (keysDown.contains(KeyEvent.VK_LEFT)
                || keysDown.contains(KeyEvent.VK_A)) {
            x -= MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION, -1);
        }
        if (keysDown.contains(KeyEvent.VK_UP)
                || keysDown.contains(KeyEvent.VK_W)) {
            y -= MOVE_SPEED;
        }

        if (keysDown.contains(KeyEvent.VK_DOWN)
                || keysDown.contains(KeyEvent.VK_S)) {
            y += MOVE_SPEED;
        }

        if (canJump && keysDown.contains(KeyEvent.VK_SPACE)) {
            y -= JUMP_SPEED;
        }

<<<<<<< HEAD
        if (keysDown.contains(KeyEvent.VK_1)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(1));
        }

        if (keysDown.contains(KeyEvent.VK_2)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(2));
        }

        if (keysDown.contains(KeyEvent.VK_ENTER)) {
            if (getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
                Entity weapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);
=======
        if (keysDown.contains(KeyEvent.VK_ENTER)) {
            if (getOwner().hasAttribute(Attribute.WEAPON)) {
                final Entity weapon = (Entity) getAttribute(Attribute.WEAPON);
>>>>>>> master
                weapon.sendMessage(MessageType.SHOOT, getOwner());
            }
        }

        getOwner().sendMessage(MessageType.APPLY_FORCE, new Vector2f(x, y));
        canJump = false;

        super.onUpdate(delta);
    }
}
