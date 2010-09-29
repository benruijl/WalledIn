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
package walledin.engine.physics;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import walledin.engine.math.Vector2f;

public class PhysicsBody {
    Body body;
    
    public PhysicsBody(Body body) {
        this.body = body;
    }

    public Vector2f getPosition() {
        return new Vector2f(body.getPosition().x, body.getPosition().y);
    }

    public Vector2f getVelocity() {
        return new Vector2f(body.getLinearVelocity().x,
                body.getLinearVelocity().y);
    }
    
    /**
     * Applies a force to the center of mass.
     * @param force Force
     */
    public void applyForce(final Vector2f force) {
        body.applyForce(new Vec2(force.getX(), force.getY()), body.getPosition());
    }
}
