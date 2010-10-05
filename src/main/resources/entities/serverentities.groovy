import walledin.engine.math.*
import walledin.engine.physics.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

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
                
                BodyDef box = new BodyDef();
                PolygonDef polygon = new PolygonDef();
                polygon.setAsBox((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f));
                polygon.density = 1.0f;
                polygon.friction = 0.0f;
                polygon.restitution = 0.2f;
                World world = PhysicsManager.getInstance().getWorld();
                Body testBox = world.createBody(box);
                testBox.createShape(polygon);
                testBox.setMassFromShapes();
                testBox.m_userData = entity.getName();
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox));
            } as EntityFunction,
            
            (Family.MAP): { entity ->
            } as EntityFunction,
            
            (Family.FOAMGUN_BULLET): { entity ->
                def destRect = new Circle(new Vector2f(8.0f, 8.0f), 8.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new StickyFoamBulletBehavior(entity));
                
                CircleDef circle = new CircleDef();
                circle.radius = destRect.getRadius();
                circle.density = 1.0f;
                circle.friction = 0.0f;
                circle.restitution = 0.2f;
                
                // don't collide with other bullets
                circle.filter.groupIndex = -1;
                
                BodyDef bodyDef = new BodyDef();
                World world = PhysicsManager.getInstance().getWorld();
                bodyDef.position = new Vec2(destRect.getPos().getX(), destRect.getPos().getY());
                final Body body = world.createBody(bodyDef);
                body.setBullet(true);
                body.createShape(circle);
                body.setUserData(entity.getName());
                body.setMassFromShapes();
                
                entity.addBehavior(new PhysicsBehavior(entity, body, false));
            } as EntityFunction,
            
            (Family.HANDGUN_BULLET): { entity ->
                def destRect = new Rectangle(0, 0, 22.0f, 11.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new BulletBehavior(entity,10));
                
                BodyDef box = new BodyDef();
                PolygonDef polygon = new PolygonDef();
                polygon.setAsBox((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f));
                polygon.density = 1.0f;
                polygon.friction = 0.0f;
                polygon.restitution = 0.2f;
                
                // don't collide with other bullets
                polygon.filter.groupIndex = -1;
                
                World world = PhysicsManager.getInstance().getWorld();
                Body testBox = world.createBody(box);
                testBox.setBullet(true);
                testBox.createShape(polygon);
                testBox.setMassFromShapes();
                testBox.m_userData = entity.getName();
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox, false));
            } as EntityFunction,
            
            (Family.FOAM_PARTICLE): { entity ->
                def destRect = new Circle(new Vector2f(16.0f, 16.0f), 16.0f);
                
                entity.addBehavior(new FoamParticleBehavior(entity));
                entity.addBehavior(new HealthBehavior(entity, 100, 80));
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                
                CircleDef circle = new CircleDef();
                circle.radius = destRect.getRadius();
                BodyDef bodyDef = new BodyDef();
                bodyDef.position = new Vec2(destRect.getPos().getX(), destRect.getPos().getY());
                World world = PhysicsManager.getInstance().getWorld();
                final Body body = world.createBody(bodyDef);
                body.createShape(circle);
                body.setUserData(entity.getName());
                
                entity.addBehavior(new PhysicsBehavior(entity, body));
            } as EntityFunction,
            
            (Family.ITEM): { entity ->
                entity.setAttribute(Attribute.PICKED_UP, false);
            } as EntityFunction,
            
            (Family.ARMOURKIT): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                
                BodyDef box = new BodyDef();
                PolygonDef polygon = new PolygonDef();
                polygon.setAsBox((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f));
                polygon.density = 1.0f;
                polygon.friction = 0.0f;
                polygon.restitution = 0.2f;
                World world = PhysicsManager.getInstance().getWorld();
                Body testBox = world.createBody(box);
                testBox.createShape(polygon);
                testBox.setMassFromShapes();
                testBox.m_userData = entity.getName();
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox));
            } as EntityFunction,
            
            (Family.HEALTHKIT): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.addBehavior(new HealthKitBehavior(entity, 10));
                
                
                BodyDef box = new BodyDef();
                PolygonDef polygon = new PolygonDef();
                polygon.setAsBox((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f));
                polygon.density = 1.0f;
                polygon.friction = 0.0f;
                polygon.restitution = 0.2f;
                World world = PhysicsManager.getInstance().getWorld();
                Body testBox = world.createBody(box);
                testBox.createShape(polygon);
                testBox.setMassFromShapes();
                testBox.m_userData = entity.getName();
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox));
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
                //entity.addBehavior(new PhysicsBehavior(entity));
            } as EntityFunction,
            
            (Family.FOAMNADE): { entity ->
                def destRect = new Rectangle(0, 0, 32.0f, 32.0f)
                //entity.addBehavior(new PhysicsBehavior(entity, 10, true, false));
                entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
                entity.setAttribute(Attribute.VELOCITY, new Vector2f());
                entity.addBehavior(new GrenadeBehavior(entity));
                
                BodyDef box = new BodyDef();
                PolygonDef polygon = new PolygonDef();
                polygon.setAsBox((float)(destRect.getWidth() / 2.0f), (float)(destRect.getHeight() / 2.0f));
                polygon.density = 1.0f;
                polygon.friction = 0.3f;
                polygon.restitution = 0.2f;
                World world = PhysicsManager.getInstance().getWorld();
                Body testBox = world.createBody(box);
                testBox.createShape(polygon);
                testBox.setMassFromShapes();
                testBox.m_userData = entity.getName();
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox));
                
                entity.addBehavior(new PhysicsBehavior(entity, testBox));
            } as EntityFunction,
        ]