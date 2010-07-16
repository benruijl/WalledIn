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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerActions;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends Behavior {
    private static final Logger LOG = Logger
            .getLogger(PlayerControlBehaviour.class);
    private static final float MOVE_SPEED = 240.0f;
    private static final float JUMP_SPEED = 8000.0f;
    private boolean canJump;
    private Set<PlayerActions> playerActions;

    public PlayerControlBehaviour(final Entity owner) {
        super(owner);
        playerActions = new HashSet<PlayerActions>();
        setAttribute(Attribute.PLAYER_ACTIONS, playerActions);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.COLLIDED) {
            final CollisionData colData = (CollisionData) data;

            if (colData.getNewPos().getY() < colData.getTheorPos().getY()) {
                canJump = true;
            }

        } else if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case PLAYER_ACTIONS:
                playerActions = (Set<PlayerActions>) getAttribute(attribute);
                break;
            default:
                break;
            }
        } else if (messageType == MessageType.DROP) {
            if (data == null) { // drop all
                // FIXME: find better way to drop all entities
                onMessage(MessageType.DROP, Attribute.ACTIVE_WEAPON);
                return;
            }

            final Attribute at = (Attribute) data;

            if (getOwner().hasAttribute(at)) {
                final Entity ent = (Entity) getAttribute(at);
                ent.sendMessage(MessageType.DROP, null);
                setAttribute(at, null);
            } else {
                LOG.warn("Trying to remove attribute " + at.toString()
                        + ", but entity " + getOwner().getName()
                        + " does not have this attribute.");
            }
        }
    }

    @Override
    public void onUpdate(final double delta) {
        float x = 0;
        float y = 0;

        if (playerActions.contains(PlayerActions.WALK_RIGHT)) {
            x += MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION_ANGLE, Float.valueOf(0));

        }

        if (playerActions.contains(PlayerActions.WALK_LEFT)) {
            x -= MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION_ANGLE, (float) Math.PI);
        }

        if (canJump && playerActions.contains(PlayerActions.JUMP)) {
            y -= JUMP_SPEED;
        }

        if (playerActions.contains(PlayerActions.SELECT_WEAPON_1)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(1));
        } else if (playerActions.contains(PlayerActions.SELECT_WEAPON_2)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(2));
        }

        // change orientation if shooting in other directory
        if (playerActions.contains(PlayerActions.SHOOT_PRIMARY)) {
            setAttribute(
                    Attribute.ORIENTATION_ANGLE,
                    ((Vector2f) getAttribute(Attribute.CURSOR_POS)).getX() < ((Vector2f) getAttribute(Attribute.POSITION))
                            .getX() ? (float) Math.PI : 0.0f);

            if (getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
                final Entity weapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);

                weapon.sendMessage(MessageType.SHOOT, getOwner());
            }
        }

        getOwner().sendMessage(MessageType.APPLY_FORCE, new Vector2f(x, y));
        canJump = false;
    }
}
