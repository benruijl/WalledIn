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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import walledin.engine.math.Vector2f;
import walledin.engine.physics.PhysicsManager;
import walledin.game.EntityManager;
import walledin.game.PlayerAction;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends AbstractBehavior implements
        ContactListener {
    private static final Logger LOG = Logger
            .getLogger(PlayerControlBehaviour.class);
    private static final float MOVE_SPEED = 500.0f;
    private static final float JUMP_SPEED = 9000.0f;
    private boolean canJump;
    private Set<PlayerAction> playerActions;

    public PlayerControlBehaviour(final Entity owner) {
        super(owner);
        playerActions = new HashSet<PlayerAction>();
        setAttribute(Attribute.PLAYER_ACTIONS, playerActions);

        /* Register the contact listener. */
        PhysicsManager.getInstance().getContactListener()
                .addListener(getOwner().getName(), this);
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case PLAYER_ACTIONS:
                playerActions = (Set<PlayerAction>) getAttribute(attribute);
                break;
            default:
                break;
            }
        } else if (messageType == MessageType.DROP) {
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

        if (playerActions.contains(PlayerAction.WALK_RIGHT)) {
            x += MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION_ANGLE, Float.valueOf(0));

        }

        if (playerActions.contains(PlayerAction.WALK_LEFT)) {
            x -= MOVE_SPEED;
            setAttribute(Attribute.ORIENTATION_ANGLE, (float) Math.PI);
        }

        if (canJump && playerActions.contains(PlayerAction.JUMP)) {
            y -= JUMP_SPEED;
        }

        if (playerActions.contains(PlayerAction.SELECT_WEAPON_1)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(1));
        } else if (playerActions.contains(PlayerAction.SELECT_WEAPON_2)) {
            getOwner().sendMessage(MessageType.SELECT_WEAPON,
                    Integer.valueOf(2));
        }

        // change orientation if shooting in other directory
        if (playerActions.contains(PlayerAction.SHOOT_PRIMARY)
                || playerActions.contains(PlayerAction.THROW_GRENADE)) {
            setAttribute(
                    Attribute.ORIENTATION_ANGLE,
                    ((Vector2f) getAttribute(Attribute.CURSOR_POS)).getX() < ((Vector2f) getAttribute(Attribute.POSITION))
                            .getX() ? (float) Math.PI : 0.0f);
        }

        if (playerActions.contains(PlayerAction.SHOOT_PRIMARY)) {
            if (getOwner().hasAttribute(Attribute.ACTIVE_WEAPON)) {
                final Entity weapon = (Entity) getAttribute(Attribute.ACTIVE_WEAPON);

                weapon.sendMessage(MessageType.SHOOT, getOwner());
            }
        }

        /* Launch a grenade. */
        if (playerActions.contains(PlayerAction.THROW_GRENADE)) {
            final Entity weapon = (Entity) getAttribute(Attribute.GRENADE_LAUNCHER);
            weapon.sendMessage(MessageType.SHOOT, getOwner());
        }

        getOwner().sendMessage(MessageType.APPLY_FORCE,
                new Vector2f(x * 10.0f, y * 10.0f));
        canJump = false;
    }

    @Override
    public void add(ContactPoint point) {
        if (point.normal.y >= -1 && point.normal.y < 0) {
            canJump = true;
        }
    }

    @Override
    public void persist(ContactPoint point) {
        if (point.normal.y >= -1 && point.normal.y < 0) {
            canJump = true;
        }
    }

    @Override
    public void remove(ContactPoint point) {
        canJump = false;
    }

    @Override
    public void result(ContactResult point) {
    }
}
