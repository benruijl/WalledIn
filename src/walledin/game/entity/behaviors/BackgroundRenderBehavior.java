package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.game.ZValues;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.math.Rectangle;

public class BackgroundRenderBehavior extends RenderBehavior {

	public BackgroundRenderBehavior(final Entity owner) {
		super(owner, ZValues.BACKGROUND);
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}
	}

	private void render(final Renderer renderer) {
		renderer.drawTexturePart("sun", new Rectangle(60, 60, 64, 64));
	}
}
