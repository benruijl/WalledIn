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
package walledin.game.entity.behaviors.render;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.ZValues;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

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
