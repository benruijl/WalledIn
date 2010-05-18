package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class ItemRenderBehavior extends RenderBehavior {
	private final String texPart;
	private final Rectangle ITEM_RECT;

	/**
	 * Creates a new item rendering behavior.
	 * @param owner Owner of behavior, an Item
	 * @param texPart
	 * @param destRect
	 */
	public ItemRenderBehavior(final Entity owner, final String texPart, final Rectangle destRect) {
		super(owner, ZValues.ITEM);

		this.texPart = texPart;
		ITEM_RECT = destRect;
	}
	
	/**
	 * Generic item renderer. It draws the texture part to the screen at the item's position.
	 * @param renderer
	 */
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
