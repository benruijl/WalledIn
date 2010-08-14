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
import walledin.engine.math.Vector2f;
import walledin.game.ZValue;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class CursorRenderBehavior extends RenderBehavior {
    /** Cursor size. */
    private static final float CURSOR_SIZE = 32;

    public CursorRenderBehavior(final Entity owner, final ZValue z) {
        super(owner, z);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.RENDER) {
            final Renderer renderer = (Renderer) data;
            // FIXME: Do this with TexturePartManager
            renderer.drawRect("tex_items", new Rectangle(0, 0, 63.5f / 1024.0f,
                    63.5f / 512.0f), new Rectangle(-16, -16, CURSOR_SIZE,
                    CURSOR_SIZE)
                    .translate((Vector2f) getAttribute(Attribute.POSITION)));
        }

        super.onMessage(messageType, data);
    }

}
