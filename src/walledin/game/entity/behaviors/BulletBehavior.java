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

import walledin.game.CollisionManager.CollisionData;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class BulletBehavior extends Behavior {

	public BulletBehavior(Entity owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.COLLIDED) {
			final CollisionData colData = (CollisionData) data;
			
			// if collided with map, destroy
			if (colData.getCollisionEntity().hasAttribute(Attribute.TILES)) 
				getOwner().remove();
		}

	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
