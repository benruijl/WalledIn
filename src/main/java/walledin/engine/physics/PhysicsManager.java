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

import org.apache.log4j.Logger;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import walledin.engine.input.Input;

public class PhysicsManager {
    private static final Logger LOG = Logger.getLogger(Input.class);
    private static final Vec2 GRAVITY = new Vec2(0, 10.0f);
    private static final float TIME_STEP = 1.0f / 60.0f;
    private static final int ITERATION = 5;
    private static PhysicsManager ref = null;
    
    private World world;
    private AABB worldRect;

    /* Ground shape, useful for testing. */
    private BodyDef groundBodyDef;
    private PolygonDef groundShapeDef;

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static PhysicsManager getInstance() {
        if (ref == null) {
            ref = new PhysicsManager();
        }

        return ref;
    }

    private PhysicsManager() {

    }

    public boolean initialize(AABB worldRect) {
        this.worldRect = worldRect;
        world = new World(worldRect, GRAVITY, true);

        groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vec2((float) 0.0, (float) -10.0));
        final Body groundBody = world.createBody(groundBodyDef);
        groundShapeDef = new PolygonDef();
        groundShapeDef.setAsBox((float) 50.0, (float) 10.0);
        groundBody.createShape(groundShapeDef);

        return true;
    }

    public void update() {
        world.step(TIME_STEP, ITERATION);
    }

}
