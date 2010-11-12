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

import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import walledin.engine.math.Rectangle;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

public class PhysicsManager {
    private static final Logger LOG = Logger.getLogger(PhysicsManager.class);
    private static final float TIME_STEP = 1.0f / 40.0f;
    private static final int ITERATION = 10;
    private static PhysicsManager ref = null;

    private DiscreteDynamicsWorld world;
    private Rectangle worldRect;
    private List<ContactListener> contactListeners;
    private List<CollisionObject> remove;

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
        contactListeners = new ArrayList<ContactListener>();
        remove = new ArrayList<CollisionObject>();
    }

    public boolean initialize(Rectangle worldRect) {
        this.worldRect = worldRect;

        // Build the broadphase
        BroadphaseInterface broadphase = new DbvtBroadphase();

        // Set up the collision configuration and dispatcher
        DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(
                collisionConfiguration);

        // The actual physics solver
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

        // The world.
        final DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(
                dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, -10, 0));

        /* Create a shape */

        /*
         * final CollisionShape groundShape = new StaticPlaneShape(new
         * Vector3f(0, 1, 0), 1);
         * 
         * final DefaultMotionState groundMotionState = new DefaultMotionState(
         * new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0,
         * -1, 0), 0)));
         * 
         * final RigidBodyConstructionInfo groundRigidBodyCI = new
         * RigidBodyConstructionInfo( 0, groundMotionState, groundShape, new
         * Vector3f(0, 0, 0)); RigidBody groundRigidBody = new
         * RigidBody(groundRigidBodyCI);
         * 
         * 
         * dynamicsWorld.addRigidBody(groundRigidBody);
         */

        world = dynamicsWorld;
        
        return true;
    }

    public DiscreteDynamicsWorld getWorld() {
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
               
        for (CollisionObject obj : world.getCollisionObjectArray()) {
            if (id == obj.getUserPointer()) {
                remove.add(obj);
                return true;
            }
        }

        return false;
    }

    public void addListener(ContactListener listener) {
        contactListeners.add(listener);
    }

    /**
     * Updates the physics simulation.
     */
    public void update() {
        world.stepSimulation(TIME_STEP, ITERATION);

        final int numManifolds = world.getDispatcher().getNumManifolds();

        for (int i = 0; i < numManifolds; i++) {
            PersistentManifold contactManifold = world.getDispatcher()
                    .getManifoldByIndexInternal(i);
            int numContacts = contactManifold.getNumContacts();
            for (int j = 0; j < numContacts; j++) {
                ManifoldPoint pt = contactManifold.getContactPoint(j);
                if (pt.getDistance() < 0.f) {
                    /* Send to all listeners. */
                    for (ContactListener listener : contactListeners) {
                        listener.processContact(pt, contactManifold);
                    }
                }
            }
        }

        /* Remove every body in the removed queue. */
        for (CollisionObject body : remove) {
            world.removeCollisionObject(body);
        }

        remove.clear();

    }
}
