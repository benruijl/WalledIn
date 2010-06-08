import walledin.util.*
import walledin.engine.*
import walledin.engine.math.*
import walledin.game.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*
import walledin.game.entity.behaviors.physics.*
import walledin.game.entity.behaviors.render.*

def textureManager = TextureManager.getInstance();
def texturePartManager = TexturePartManager.getInstance()

def itemsTexture = "tex_items"
textureManager.loadFromURL(Utils.getClasspathURL("game.png"), itemsTexture);

texturePartManager.createTexturePart("foambullet", itemsTexture, new Rectangle(334, 213, 32, 32))
texturePartManager.createTexturePart("handgunbullet", itemsTexture, new Rectangle(205, 152, 44, 22))
texturePartManager.createTexturePart("armorkit", itemsTexture, new Rectangle(384, 64, 64, 64))
texturePartManager.createTexturePart("healthkit", itemsTexture, new Rectangle(320, 64, 64, 64))
texturePartManager.createTexturePart("handgun", itemsTexture, new Rectangle(64, 132, 120, 63))
texturePartManager.createTexturePart("foamgun", itemsTexture, new Rectangle(64, 194, 240, 63))

[
(Family.PLAYER): { Entity entity ->
    entity.setAttribute(Attribute.ORIENTATION, 1); // start looking to
    // the right

    entity.addBehavior(new PlayerAnimationBehavior(entity));
    // TODO: we should be able to define player texture here
    entity.addBehavior(new PlayerRenderBehavior(entity));
    // spatial behavior does the interpolation in between server messages
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new PlayerParentBehavior(entity));

    // FIXME correct the drawing instead of the hack the bounding box
    entity.setAttribute(Attribute.BOUNDING_RECT, new Rectangle(0, 0, 44, 43));
} as EntityFunction,

(Family.BACKGROUND): { Entity entity ->
    entity.addBehavior(new BackgroundRenderBehavior(entity));
} as EntityFunction,

(Family.MAP): { Entity entity ->
    entity.addBehavior(new MapRenderBehavior(entity));
} as EntityFunction,

(Family.FOAMGUN_BULLET): { Entity entity ->
    def destRect = new Rectangle(0, 0, 16, 16)
    entity.addBehavior(new ItemRenderBehavior(entity, "foambullet", destRect));
    // spatial behavior does the interpolation in between server messages
    entity.addBehavior(new SpatialBehavior(entity));
} as EntityFunction,

(Family.HANDGUN_BULLET): { Entity entity ->
    def destRect = new Rectangle(0, 0, 22, 11)
    entity.addBehavior(new ItemRenderBehavior(entity, "handgunbullet", destRect));
    // spatial behavior does the interpolation in between server messages
    entity.addBehavior(new SpatialBehavior(entity));
} as EntityFunction,

(Family.FOAM_PARTICLE): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new ItemRenderBehavior(entity, "foambullet", destRect));
} as EntityFunction,

(Family.ARMOURKIT): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new ItemRenderBehavior(entity, "armorkit", destRect));
} as EntityFunction,

(Family.HEALTHKIT): { Entity entity ->
    def destRect = new Rectangle(0, 0, 32, 32)
    entity.addBehavior(new ItemRenderBehavior(entity, "healthkit", destRect));
} as EntityFunction,

(Family.HANDGUN): { Entity entity ->
    def destRect = new Rectangle(0, 0, 40, 21)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new WeaponRenderBehavior(entity, "handgun", destRect));
} as EntityFunction,

(Family.FOAMGUN): { Entity entity ->
    def destRect = new Rectangle(0, 0, 80, 21)
    entity.addBehavior(new SpatialBehavior(entity));
    entity.addBehavior(new WeaponRenderBehavior(entity, "foamgun", destRect));
} as EntityFunction,

(Family.CURSOR): { Entity entity ->
    entity.addBehavior(new CursorRenderBehavior(entity, ZValues.CURSOR));
} as EntityFunction,

]