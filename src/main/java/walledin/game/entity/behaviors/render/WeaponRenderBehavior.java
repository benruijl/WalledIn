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
import walledin.util.Utils;

public class WeaponRenderBehavior extends RenderBehavior {
    private final String texPart;
    private final Rectangle weaponRect;

    /**
     * Creates a new item rendering behavior.
     * 
     * @param owner
     *            Owner of behavior, a Weapon
     * @param texPart
     * @param destRect
     */
    public WeaponRenderBehavior(final Entity owner, final String texPart,
            final Rectangle destRect) {
        super(owner, ZValue.WEAPON);

        this.texPart = texPart;
        weaponRect = destRect;
    }

    /**
     * Generic weapon renderer. It draws the texture part to the screen at the
     * weapon's position.
     * 
     * @param renderer
     */
    private void render(final Renderer renderer) {
        renderer.pushMatrix();

        final Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
        renderer.translate(pos);

        if (Utils
                .getCircleHalf((Float) getAttribute(Attribute.ORIENTATION_ANGLE)) == -1) {
            renderer.translate(new Vector2f(-23, 0));
            renderer.scale(new Vector2f(-1, 1));

        }

        renderer.drawTexturePart(texPart, weaponRect);

        renderer.popMatrix();
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {

        if (messageType == MessageType.RENDER) {
            render((Renderer) data);
        }

        super.onMessage(messageType, data);
    }

}
