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
package walledin.game.entity.behaviors;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PlayerControlBehaviour extends SpatialBehavior {
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

			if (colData.getNewPos().getY() < colData.getTheorPos().getY())
				canJump = true;
		} else if (messageType == MessageType.ATTRIBUTE_SET) {
			final Attribute attribute = (Attribute) data;
			switch (attribute) {
			case KEYS_DOWN:
				keysDown = (Set<Integer>) getAttribute(attribute);
				break;
			}
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

		if (keysDown.contains(KeyEvent.VK_ENTER)) {
			if (getOwner().hasAttribute(Attribute.WEAPON))
			{
				Entity weapon = (Entity) getAttribute(Attribute.WEAPON);
				weapon.sendMessage(MessageType.SHOOT, getOwner());
			}
		}

		getOwner().sendMessage(MessageType.APPLY_FORCE, new Vector2f(x, y));
		canJump = false;
		
		super.onUpdate(delta);
	}
}
