import walledin.util.*
import walledin.engine.math.*
import walledin.game.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*
import walledin.game.entity.behaviors.physics.*

[
(Family.PLAYER): { Entity entity ->
    entity.setAttribute(Attribute.ORIENTATION, 1); // start looking to
    // the right

    entity.addBehavior(new HealthBehavior(entity, 100, 100));
    entity.addBehavior(new PlayerControlBehaviour(entity));
    entity.addBehavior(new PlayerParentBehavior(entity));
    entity.addBehavior(new PhysicsBehavior(entity));
    entity.addBehavior(new PlayerWeaponInventoryBehavior(entity));

    // FIXME correct the drawing instead of the hack the bounding box
    entity.setAttribute(Attribute.BOUNDING_RECT,
    new Rectangle(0, 0, 44, 43));
} as EntityFunction,

(Family.MAP): { Entity entity ->
    entity.setAttribute(Attribute.RENDER_TILE_SIZE, 32f);
} as EntityFunction,

(Family.FOAMGUN_BULLET): { Entity entity ->
    def destRect = new Rectangle(0, 0, 16, 16)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new PhysicsBehavior(entity, false, false));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
    entity.addBehavior(new FoamBulletBehavior(entity));
} as EntityFunction,

(Family.HANDGUN_BULLET): { Entity entity ->
    def destRect = new Rectangle(0, 0, 22, 11)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new PhysicsBehavior(entity, false, false));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
    entity.addBehavior(new FoamBulletBehavior(entity));
} as EntityFunction,

(Family.FOAM_PARTICLE): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
} as EntityFunction,

(Family.ARMOURKIT): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new PhysicsBehavior(entity));

    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
} as EntityFunction,

(Family.HEALTHKIT): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new PhysicsBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);

    entity.addBehavior(new HealthKitBehavior(entity, 10));
} as EntityFunction,

(Family.HANDGUN): { Entity entity ->
    def destRect = new Rectangle(0, 0, 40, 21)
    entity.addBehavior(new SpatialBehavior(entity));
    // entity.addBehavior(new PhysicsBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
    entity.addBehavior(new WeaponBehavior(entity, 10, Family.HANDGUN_BULLET));
} as EntityFunction,

(Family.FOAMGUN): { Entity entity ->
    def destRect = new Rectangle(0, 0, 80, 21)
    entity.addBehavior(new SpatialBehavior(entity));
    // entity.addBehavior(new PhysicsBehavior(entity));
    entity.setAttribute(Attribute.BOUNDING_RECT, destRect);
    entity.addBehavior(new WeaponBehavior(entity, 10, Family.FOAMGUN_BULLET));
} as EntityFunction,
]