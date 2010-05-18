package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class PlayerRenderBehavior extends RenderBehavior {
	private static final Rectangle EYE_RIGHT_RECT = new Rectangle(55, 30, 20,
			32);
	private static final Rectangle EYE_LEFT_RECT = new Rectangle(45, 30, 20, 32);
	private static final Rectangle BODY_RECT = new Rectangle(0, 0, 96, 96);
	private static final String PLAYER_EYES = "player_eyes";
	private static final String PLAYER_FOOT = "player_foot";
	private static final String PLAYER_BACKGROUND_FOOT = "player_background_foot";
	private static final String PLAYER_BODY = "player_body";
	private static final String PLAYER_BACKGROUND = "player_background";
	private Vector2f pos;
	private final Vector2f scale;

	public PlayerRenderBehavior(final Entity owner) {
		super(owner, ZValues.PLAYER);

		scale = new Vector2f(0.5f, 0.5f); // standard scale
	}

	private void render(final Renderer renderer) {
		renderer.pushMatrix();

		pos = getOwner().getAttribute(Attribute.POSITION);
		renderer.translate(pos);

		renderer.scale(scale);

		if (((Integer) getAttribute(Attribute.ORIENTATION)).intValue() == -1) {
			renderer.translate(new Vector2f(scale.getX() * 96 * 2, 0));
			renderer.scale(new Vector2f(-1, 1));
		}

		final float footPos = getAttribute(Attribute.WALK_ANIM_FRAME);

		renderer.drawTexturePart(PLAYER_BACKGROUND, BODY_RECT);
		renderer.drawTexturePart(PLAYER_BODY, BODY_RECT);
		renderer.drawTexturePart(PLAYER_EYES, EYE_LEFT_RECT);
		renderer.drawTexturePart(PLAYER_EYES, EYE_RIGHT_RECT);

		// render foot
		final float footX = (float) (Math.cos(footPos) * 8.0 + 35.0);
		final Rectangle footRect = new Rectangle(footX, 60, 96, 32);
		renderer.drawTexturePart(PLAYER_BACKGROUND_FOOT, footRect);
		renderer.drawTexturePart(PLAYER_FOOT, footRect);

		renderer.popMatrix();
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}
	}
}
