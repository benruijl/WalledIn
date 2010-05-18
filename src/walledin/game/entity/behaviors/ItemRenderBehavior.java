package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class ItemRenderBehavior extends RenderBehavior {
	private final String texPart;
	private final Rectangle ITEM_RECT;

	public ItemRenderBehavior(final Entity owner, final String texPart,
			final Rectangle destRect) {
		super(owner, ZValues.ITEM);

		this.texPart = texPart;
		ITEM_RECT = destRect;
	}

	private void render(final Renderer renderer) {
		renderer.drawTexturePart(texPart, ITEM_RECT
				.translate((Vector2f) getAttribute(Attribute.POSITION)));
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {

		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}

		super.onMessage(messageType, data);
	}

}
