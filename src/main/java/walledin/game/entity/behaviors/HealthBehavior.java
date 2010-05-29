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

import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthBehavior extends Behavior {
	private int health;
	private final int maxHealth;

	public HealthBehavior(final Entity owner, final int maxHealth,
			final int curHealth) {
		super(owner);

		health = curHealth;
		this.maxHealth = maxHealth;

		setAttribute(Attribute.HEALTH, health);
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.RESTORE_HEALTH) {
			final int hp = (Integer) data;
			if (health + hp > maxHealth) {
				health = maxHealth;
			} else {
				health += hp;
			}

			setAttribute(Attribute.HEALTH, health);
		}

	}

	@Override
	public void onUpdate(final double delta) {
		// TODO Auto-generated method stub

	}

}
