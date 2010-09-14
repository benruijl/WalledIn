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
package walledin.engine;

import org.apache.log4j.Logger;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

import com.sun.opengl.util.texture.Texture;

/**
 * 
 * @author ben
 */
public final class TexturePartManager extends
        ResourceManager<String, TexturePart> {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TexturePartManager.class);
    private static final TexturePartManager INSTANCE = new TexturePartManager();

    private TexturePartManager() {

    }

    public static TexturePartManager getInstance() {
        return INSTANCE;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public boolean createTexturePart(final String texturePartID,
            final String textureID, final Rectangle rectangle) {
        final Texture texture = TextureManager.getInstance().get(textureID);
        final float width = texture.getWidth();
        final float height = texture.getHeight();
        final Vector2f scale = new Vector2f(1 / width, 1 / height);
        final Rectangle scaledRectangle = rectangle.scaleAll(scale);
        final TexturePart part = new TexturePart(texture, scaledRectangle);
        return put(texturePartID, part);
    }
}
