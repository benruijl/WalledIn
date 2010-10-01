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
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;

import walledin.engine.math.Rectangle;

public class PhysicsManager {
    private static final Logger LOG = Logger.getLogger(PhysicsManager.class);
    private static final Vec2 GRAVITY = new Vec2(0, 40.0f);
    private static final float TIME_STEP = 1.0f / 60.0f;
    private static final int ITERATION = 5;
    private static PhysicsManager ref = null;

    private World world;
    private AABB worldRect;
    private GeneralContactListener contactListener;

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
        contactListener = new GeneralContactListener();

    }

    public void addContactListener(Object id, ContactListener listener) {
        contactListener.addListener(id, listener);
    }

    public void removeContactListener(ContactListener listener) {
        contactListener.removeListener(listener);
    }

    public boolean initialize(Rectangle worldRect) {
        this.worldRect = new AABB(new Vec2(worldRect.getLeft(),
                worldRect.getTop()), new Vec2(worldRect.getRight(),
                worldRect.getBottom()));
        world = new World(this.worldRect, GRAVITY, true);
        world.setContactListener(contactListener);

        createStaticBody(new Rectangle(0, worldRect.getBottom(),
                worldRect.getWidth(), 10));

        // createStaticBody(new Rectangle(0, 40, 300, 70));

        return true;
    }

    /**
     * Creates a static box from a bounding rectangle.
     * 
     * @param rect
     *            Rectangle
     */
    public void createStaticBody(Rectangle rect) {
        BodyDef box = new BodyDef();
        box.position.set(rect.getLeft() + rect.getWidth() / 2.0f, rect.getTop()
                + rect.getHeight() / 2.0f);
        PolygonDef polygon = new PolygonDef();
        polygon.setAsBox(rect.getWidth() / 2.0f, rect.getHeight() / 2.0f);
        final Body testBox = world.createBody(box);
        testBox.createShape(polygon);
    }

    public PhysicsBody addBody(Rectangle rect, Object userData) {
        /* Add dummy object */
        BodyDef box = new BodyDef();
        box.position.set(rect.getLeft() + rect.getWidth() / 2.0f, rect.getTop()
                + rect.getHeight() / 2.0f);
        PolygonDef polygon = new PolygonDef();
        polygon.setAsBox(rect.getWidth() / 2.0f, rect.getHeight() / 2.0f);
        polygon.density = 1.0f;
        polygon.friction = 0.3f;
        polygon.restitution = 0.2f;
        Body testBox = world.createBody(box);
        testBox.createShape(polygon);
        testBox.setMassFromShapes();
        testBox.m_userData = userData;

        return new PhysicsBody(testBox);
    }

    public void update() {
        world.step(TIME_STEP, ITERATION);
    }

}
