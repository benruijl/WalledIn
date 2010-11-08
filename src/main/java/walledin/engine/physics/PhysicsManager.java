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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;

public class PhysicsManager {
    private static final Logger LOG = Logger.getLogger(PhysicsManager.class);
    private static final float TIME_STEP = 1.0f / 40.0f;
    private static final int ITERATION = 10;
    private static PhysicsManager ref = null;

    private World world;
    private AABB worldRect;
    private GeneralContactListener contactListener;
    private List<Body> remove;

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
        remove = new ArrayList<Body>();

    }

    public GeneralContactListener getContactListener() {
        return contactListener;
    }

    public boolean initialize(Rectangle worldRect) {
        this.worldRect = new AABB(new Vec2(worldRect.getLeft(), worldRect
                .getTop()), new Vec2(worldRect.getRight(), worldRect
                .getBottom()));
        world = new World(this.worldRect, new Vec2(), true);
        world.setContactListener(contactListener);

        addStaticBody(new Rectangle(0, worldRect.getBottom(), worldRect
                .getWidth(), 10), null);
        return true;
    }

    /**
     * Creates a static box from a bounding rectangle.
     * 
     * @param rect
     *            Rectangle
     * @param userData
     *            User data
     * @return Created body
     */
    public PhysicsBody addStaticBody(Rectangle rect, Object userData) {
        BodyDef box = new BodyDef();
        box.position.set(rect.getLeft() + rect.getWidth() / 2.0f, rect.getTop()
                + rect.getHeight() / 2.0f);
        PolygonDef polygon = new PolygonDef();
        polygon.setAsBox(rect.getWidth() / 2.0f, rect.getHeight() / 2.0f);
        final Body testBox = world.createBody(box);
        testBox.createShape(polygon);
        testBox.setUserData(userData);

        return new PhysicsBody(testBox);
    }

    public PhysicsBody addBody(Rectangle rect, Object userData) {
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

    public World getWorld() {
        return world;
    }

    /**
     * Adds a body to the remove queue.
     * 
     * @param id
     *            ID of the body
     * @return True if body exists, else false
     */
    public boolean addToRemoveQueue(Object id) {
        if (world == null) {
            return false;
        }

        Body currentBody = world.getBodyList();
        while (currentBody != null) {
            if (id == currentBody.getUserData()) {
                remove.add(currentBody);
                return true;
            }

            currentBody = currentBody.getNext();
        }

        return false;
    }

    /**
     * Updates the physics simulation.
     */
    public void update() {
        world.step(TIME_STEP, ITERATION);

        contactListener.update();

        /* Remove every body in the removed queue. */
        for (Body body : remove) {
            world.destroyBody(body);
        }

        remove.clear();
    }

}
