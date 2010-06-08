package walledin.game.entity.behaviors.render;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class CursorRenderBehavior extends RenderBehavior {

    public CursorRenderBehavior(final Entity owner, final ZValues z) {
        super(owner, z);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.RENDER) {
            final Renderer renderer = (Renderer) data;
            // FIXME: Do this with texturepartmanager
            renderer.drawRect("tex_items", new Rectangle(0, 0, 63.5f / 1024.0f,
                    63.5f / 512.0f), new Rectangle(0, 0, 32, 32)
                    .translate((Vector2f) getAttribute(Attribute.POSITION)));
        }

        super.onMessage(messageType, data);
    }

}
