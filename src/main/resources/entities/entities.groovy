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
(Family.HEALTHKIT): { Entity entity ->
	entity.addBehavior(new ItemRenderBehavior(entity, "", new Rectangle(0, 0, 10, 10)));
} as EntityFunction
]