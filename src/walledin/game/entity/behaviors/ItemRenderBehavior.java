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
