import walledin.engine.math.*
import walledin.engine.physics.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*

import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/* TODO: change variable names! */

[
            (Family.PLAYER): { entity ->
                entity.setAttribute(Attribute.ORIENTATION_ANGLE, 0.0f);
                entity.addBehavior(new HealthBehavior(entity, 100, 100));
                entity.addBehavior(new PlayerControlBehaviour(entity));
                entity.addBehavior(new PlayerParentBehavior(entity));
                entity.addBehavior(new PlayerWeaponInventoryBehavior(entity));
                
                // create grenade launcher
                def grenLauncher = entity.getEntityManager().create(Family.GRENADE_LAUNCHER);
                entity.setAttribute(Attribute.GRENADE_LAUNCHER, grenLauncher);
                grenLauncher.sendMessage(MessageType.PICK_UP, entity);
                
                // FIXME correct the drawing instead of the hacking the bounding box
                def destRect = new Rectangle(0, 0, 44.0f, 43.0f);
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                
                CollisionShape shape = new BoxShape(new Vector3f((float)(destRect.getWidth() / 2.0f), 
                (float)(destRect.getHeight() / 2.0f), 2));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb));
            } as EntityFunction,
            
            (Family.MAP): { entity ->
            } as EntityFunction,
            
            (Family.FOAMGUN_BULLET): { entity ->
                def destRect = new Circle(new Vector2f(8.0f, 8.0f), 8.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new FoamBulletBehavior(entity));
                
                /*  CircleDef circle = new CircleDef();
         circle.radius = destRect.getRadius();
         circle.density = 0.01f;
         circle.friction = 0.0f;
         circle.restitution = 0.2f;
         // don't collide with other bullets
         circle.filter.groupIndex = -1;
         BodyDef bodyDef = new BodyDef();
         World world = PhysicsManager.getInstance().getWorld();
         bodyDef.position = new Vec2(destRect.getPos().getX(), destRect.getPos().getY());
         final Body body = world.createBody(bodyDef);
         body.m_linearDamping = 0.0f;
         body.setBullet(true);
         body.createShape(circle);
         body.setUserData(entity.getName());
         body.setMassFromShapes();*/
                
                CollisionShape shape = new CylinderShape(new Vector3f((float)(destRect.getRadius() / 2.0f), 
                (float)(destRect.getRadius() / 2.0f), 0));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb, false));
            } as EntityFunction,
            
            (Family.HANDGUN_BULLET): { entity ->
                def destRect = new Rectangle(0, 0, 22.0f, 11.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new BulletBehavior(entity,10));
                
                CollisionShape shape = new BoxShape(new Vector3f((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f), 0));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb, false));
            } as EntityFunction,
            
            (Family.FOAM_PARTICLE): { entity ->
                def destRect = new Circle(new Vector2f(16.0f, 16.0f), 16.0f);
                
                entity.addBehavior(new FoamParticleBehavior(entity));
                entity.addBehavior(new HealthBehavior(entity, 100, 80));
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                
                /*   CircleDef circle = new CircleDef();
         circle.radius = destRect.getRadius();
         BodyDef bodyDef = new BodyDef();
         bodyDef.position = new Vec2(destRect.getPos().getX(), destRect.getPos().getY());
         World world = PhysicsManager.getInstance().getWorld();
         final Body body = world.createBody(bodyDef);
         body.createShape(circle);
         body.setUserData(entity.getName());
         entity.addBehavior(new PhysicsBehavior(entity, body));*/
            } as EntityFunction,
            
            (Family.ITEM): { entity ->
                entity.setAttribute(Attribute.PICKED_UP, false);
            } as EntityFunction,
            
            (Family.ARMOURKIT): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                
                CollisionShape shape = new BoxShape(new Vector3f((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f), 0));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb));
            } as EntityFunction,
            
            (Family.HEALTHKIT): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new HealthKitBehavior(entity, 10));
                
                
                CollisionShape shape = new BoxShape(new Vector3f((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f), 0));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb));
            } as EntityFunction,
            
            (Family.HANDGUN): { entity ->
                def destRect = new Rectangle(0, 0, 40.0f, 21.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                entity.addBehavior(new WeaponBehavior(entity, 10, Family.HANDGUN_BULLET));
            } as EntityFunction,
            
            (Family.GRENADE_LAUNCHER): { entity ->
                entity.addBehavior(new WeaponBehavior(entity, 10, 200000.0f, Family.FOAMNADE));
            } as EntityFunction,
            
            (Family.FOAMGUN): { entity ->
                def destRect = new Rectangle(0, 0, 80.0f, 21.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                entity.addBehavior(new WeaponBehavior(entity, 4, Family.FOAMGUN_BULLET));
                
                // DO NOT add physics behavior for guns. A body is created in the WeaponBehavior.
            } as EntityFunction,
            
            (Family.FOAMNADE): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                //entity.addBehavior(new PhysicsBehavior(entity, 10, true, false));
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                entity.addBehavior(new GrenadeBehavior(entity));
                
                CollisionShape shape = new BoxShape(new Vector3f((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f), 0));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                        new Vector3f(0, 0, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(entity.getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager.getInstance().getWorld().addRigidBody(rb);
                
                entity.addBehavior(new PhysicsBehavior(entity, rb));
            } as EntityFunction,
        ]