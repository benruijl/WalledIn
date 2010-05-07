package walledin.game.entity.behaviors;

import walledin.engine.Rectangle;
import walledin.engine.Renderer;
import walledin.engine.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class RenderPlayerBehavior extends RenderBehavior {
	private final String texture;
	private Vector2f pos;
	private Vector2f scale;

	public RenderPlayerBehavior(Entity owner, String texture) {
		super(owner);
		this.texture = texture;

		scale = new Vector2f(0.5f, 0.5f); // standard scale
	}

	private void render(Renderer renderer) {
		renderer.pushMatrix();

		pos = getOwner().getAttribute(Attribute.POSITION);
		renderer.translate(pos);

		renderer.scale(scale);

		if (((Integer) getAttribute(Attribute.ORIENTATION)).intValue() == -1) {
			renderer.scale(new Vector2f(-1, 1));
		}

		float footPos = getAttribute(Attribute.WALK_ANIM_FRAME);

		renderer.drawRect(texture, new Rectangle(96 / 256.0f, 0, 96 / 256.0f,
				96 / 128.0f), new Rectangle(0, 0, 96, 96)); // render background
		renderer.drawRect(texture,
				new Rectangle(0, 0, 96 / 256.0f, 96 / 128.0f), new Rectangle(0,
						0, 96, 96)); // draw body
		renderer.drawRect(texture, new Rectangle(70 / 256.0f, 96 / 128.0f,
				20 / 256.0f, 32 / 128.0f), new Rectangle(45, 30, 20, 32)); // eyes
		renderer.drawRect(texture, new Rectangle(70 / 256.0f, 96 / 128.0f,
				20 / 256.0f, 32 / 128.0f), new Rectangle(55, 30, 20, 32)); // eyes

		// render foot
		renderer
				.drawRect(texture, new Rectangle(192 / 256.0f, 64 / 128.0f,
						96 / 256.0f, 32 / 128.0f), new Rectangle(
						(float) (java.lang.Math.cos(footPos) * 8.0 + 35.0), 60,
						96, 32));

		renderer
				.drawRect(texture, new Rectangle(192 / 256.0f, 32 / 128.0f,
						96 / 256.0f, 32 / 128.0f), new Rectangle(
						(float) (java.lang.Math.cos(footPos) * 8.0 + 35.0), 60,
						96, 32)); // foot

		renderer.popMatrix();
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}
	}
}
