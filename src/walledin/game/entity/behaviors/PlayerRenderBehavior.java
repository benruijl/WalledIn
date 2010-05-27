/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

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
