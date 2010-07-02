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

texturePartManager.createTexturePart("foambullet", itemsTexture, new Rectangle(340, 212, 24, 24))
texturePartManager.createTexturePart("handgunbullet", itemsTexture, new Rectangle(205, 152, 44, 22))
texturePartManager.createTexturePart("armorkit", itemsTexture, new Rectangle(384, 64, 64, 64))
texturePartManager.createTexturePart("healthkit", itemsTexture, new Rectangle(320, 64, 64, 64))
texturePartManager.createTexturePart("handgun", itemsTexture, new Rectangle(64, 132, 120, 63))
texturePartManager.createTexturePart("foamgun", itemsTexture, new Rectangle(64, 194, 240, 63))

[
(Family.PLAYER): { Entity entity ->
    entity.setAttribute(Attribute.ORIENTATION_ANGLE, 0.0f); // start looking to
    // the right
    entity.addBehavior(new PlayerAnimationBehavior(entity));
    // TODO: we should be able to define player texture here
    entity.addBehavior(new PlayerRenderBehavior(entity));
    entity.addBehavior(new PlayerParentBehavior(entity));
} as EntityFunction,

(Family.BACKGROUND): { Entity entity ->
    entity.addBehavior(new BackgroundRenderBehavior(entity));
} as EntityFunction,

(Family.MAP): { Entity entity ->
    entity.addBehavior(new MapRenderBehavior(entity));
} as EntityFunction,

(Family.FOAMGUN_BULLET): { Entity entity ->
    def destRect = new Circle(new Vector2f(8, 8), 8)
    entity.addBehavior(new ItemRenderBehavior(entity, "foambullet", destRect));
} as EntityFunction,

(Family.HANDGUN_BULLET): { Entity entity ->
    def destRect = new Rectangle(0, 0, 22, 11)
    entity.addBehavior(new ItemRenderBehavior(entity, "handgunbullet", destRect));
} as EntityFunction,

(Family.FOAM_PARTICLE): { Entity entity ->
    def destRect = new Circle(new Vector2f(16, 16), 16)
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
    entity.addBehavior(new WeaponRenderBehavior(entity, "handgun", destRect));
} as EntityFunction,

(Family.FOAMGUN): { Entity entity ->
    def destRect = new Rectangle(0, 0, 80, 21)
    entity.addBehavior(new WeaponRenderBehavior(entity, "foamgun", destRect));
} as EntityFunction,

(Family.CURSOR): { Entity entity ->
    entity.addBehavior(new CursorRenderBehavior(entity, ZValues.CURSOR));
} as EntityFunction,

]