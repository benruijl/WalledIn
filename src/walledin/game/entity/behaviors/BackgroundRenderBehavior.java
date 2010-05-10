package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.ZValues;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class BackgroundRenderBehavior extends RenderBehavior {

	public BackgroundRenderBehavior(Entity owner) {
		super(owner, ZValues.BACKGROUND);
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}
	}

	private void render(Renderer renderer) {
		renderer.drawTexturePart("sun", new Rectangle(60, 60, 64, 64));
	}
}
