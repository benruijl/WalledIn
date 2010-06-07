import walledin.engine.*
import walledin.engine.math.*
import walledin.game.entity.*
import walledin.game.entity.behaviors.logic.*
import walledin.game.entity.behaviors.physics.*
import walledin.game.entity.behaviors.render.*
import walledin.game.*

[
(Family.PLAYER): { Entity entity ->
        entity.setAttribute(Attribute.ORIENTATION, 1); // start looking to
        // the right

        player.addBehavior(new PlayerAnimationBehavior(player));
        player.addBehavior(new PlayerRenderBehavior(player));
        // spatial behavior does the interpolation in between server messages
        player.addBehavior(new SpatialBehavior(player));
        player.addBehavior(new PlayerParentBehavior(player));

        // FIXME correct the drawing instead of the hack the bounding box
        player.setAttribute(Attribute.BOUNDING_RECT,
                new Rectangle(0, 0, 44, 43));
} as EntityFunction,

(Family.BACKGROUND): { Entity entity ->
    entity.addBehavior(new BackgroundRenderBehavior(entity));
} as EntityFunction,

(Family.GAMEMAP): { Entity entity ->
    entity.addBehavior(new MapRenderBehavior(entity));
} as EntityFunction,

(  Family.FOAM_BULLET): { Entity entity ->
TexturePartManager.getInstance().createTexturePart(texPartName,
                        texName, new Rectangle(x, y, width, height));
                        
         new Rectangle(0, 0, destWidth, destHeight)
        entity.addBehavior(new ItemRenderBehavior(entity, texPart, destRect));
        // spatial behavior does the interpolation in between server messages
        entity.addBehavior(new SpatialBehavior(entity));
} as EntityFunction,
]

    private Entity createBullet(final String texPart, final Rectangle destRect,
            final Element el, final Entity bl) {
        bl.addBehavior(new ItemRenderBehavior(bl, texPart, destRect));
        // spatial behavior does the interpolation in between server messages
        bl.addBehavior(new SpatialBehavior(bl));
        return bl;
    }

    private Entity createFoamParticle(final String texPart,
            final Rectangle destRect, final Element el, final Entity bl) {
        bl.addBehavior(new ItemRenderBehavior(bl, texPart, destRect));
        return bl;
    }

    private Entity createArmorKit(final String texPart,
            final Rectangle destRect, final Element el, final Entity ak) {
        ak.addBehavior(new ItemRenderBehavior(ak, texPart, destRect));
        return ak;
    }

    private Entity createHealthKit(final String texPart,
            final Rectangle destRect, final Element el, final Entity hk) {
        hk.addBehavior(new ItemRenderBehavior(hk, texPart, destRect));
        return hk;
    }

    private Entity createWeapon(final String texPart, final Rectangle destRect,
            final Element el, final Entity hg) {
        hg.addBehavior(new SpatialBehavior(hg));
        hg.addBehavior(new WeaponRenderBehavior(hg, texPart, destRect));
        return hg;
    }