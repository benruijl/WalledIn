import walledin.engine.math.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*
import walledin.game.entity.behaviors.physics.*

[
(Family.PLAYER): { entity ->
    entity.setAttribute(Attribute.ORIENTATION_ANGLE, 0.0f); // start looking to
    // the right

    entity.addBehavior(new HealthBehavior(entity, 100, 100));
    entity.addBehavior(new PlayerControlBehaviour(entity));
    entity.addBehavior(new PlayerParentBehavior(entity));
    entity.addBehavior(new PlayerWeaponInventoryBehavior(entity));

    // FIXME correct the drawing instead of the hack the bounding box
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY,
    new Rectangle(0, 0, 44, 43));
} as EntityFunction,

(Family.MAP): { entity ->
} as EntityFunction,

(Family.FOAMGUN_BULLET): { entity ->
    def destRect = new Circle(new Vector2f(8, 8), 8)
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.addBehavior(new StickyFoamBulletBehavior(entity));
} as EntityFunction,

(Family.HANDGUN_BULLET): { entity ->
    def destRect = new Rectangle(0, 0, 22, 11)
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.addBehavior(new BulletBehavior(entity,10));
} as EntityFunction,

(Family.FOAM_PARTICLE): { entity ->
    def destRect = new Circle(new Vector2f(16, 16), 16)
    entity.addBehavior(new PhysicsBehavior(entity, 2e4, false, false));
    entity.addBehavior(new StaticObjectCollisionBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.setAttribute(Attribute.VELOCITY, new Vector2f());
} as EntityFunction,

(Family.ITEM): { entity ->
    entity.setAttribute(Attribute.PICKED_UP, false);
} as EntityFunction,

(Family.ARMOURKIT): { entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
} as EntityFunction,

(Family.HEALTHKIT): { entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.addBehavior(new HealthKitBehavior(entity, 10));
} as EntityFunction,

(Family.HANDGUN): { entity ->
    def destRect = new Rectangle(0, 0, 40, 21)
    // entity.addBehavior(new PhysicsBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.setAttribute(Attribute.VELOCITY, new Vector2f());
    entity.addBehavior(new WeaponBehavior(entity, 10, Family.HANDGUN_BULLET));
} as EntityFunction,

(Family.FOAMGUN): { entity ->
    def destRect = new Rectangle(0, 0, 80, 21)
    // entity.addBehavior(new PhysicsBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_GEOMETRY, destRect);
    entity.setAttribute(Attribute.VELOCITY, new Vector2f());
    entity.addBehavior(new WeaponBehavior(entity, 4, Family.FOAMGUN_BULLET));
} as EntityFunction,
]