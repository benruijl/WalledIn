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

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class PhysicsBehavior extends Behavior {
	private static final Logger LOG = Logger.getLogger(PhysicsBehavior.class);
	private final Vector2f gravity; // acceleration of gravity
	private final float frictionCoefficient; // part of the velocity that is
	// kept
	private Vector2f acceleration = new Vector2f(0, 0);


	public PhysicsBehavior(Entity owner) {
		this(owner, true, true);
	}

	public PhysicsBehavior(Entity owner, boolean doGravity, boolean doFriction) {
		super(owner);
		if (doGravity) {
			gravity = new Vector2f(0, 300.0f);
		} else {
			gravity = new Vector2f(0, 0);
		}
		if (doFriction) {
			frictionCoefficient = 0.02f;
		} else {
			frictionCoefficient = 0;
		}
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.APPLY_FORCE) {
			acceleration = acceleration.add((Vector2f) data);
		}

	}

	@Override
	public void onUpdate(double delta) {
		Vector2f velCur = (Vector2f) getAttribute(Attribute.VELOCITY);
		Vector2f posCur = (Vector2f) getAttribute(Attribute.POSITION);

		acceleration = acceleration.add(gravity);

    	// add friction
		acceleration = acceleration.add(new Vector2f(-Math.signum(velCur.x)
				* velCur.x * velCur.x * frictionCoefficient, -Math.signum(velCur.y)
				* velCur.y * velCur.y * frictionCoefficient));

		Vector2f velNew = velCur.add(acceleration.scale((float) delta));
		Vector2f posNew = posCur.add(velNew.scale((float) delta));

		setAttribute(Attribute.VELOCITY, velNew);
		setAttribute(Attribute.POSITION, posNew);

		acceleration = new Vector2f(0, 0);
	}

}
