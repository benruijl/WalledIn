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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.media.opengl.GLException;

import org.apache.log4j.Logger;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 * 
 * @author ben
 */
public class TextureManager extends ResourceManager<String, Texture> {
    private final static Logger LOG = Logger.getLogger(TextureManager.class);
    private static final TextureManager INSTANCE = new TextureManager();

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    private TextureManager() {

    }

    private String generateUniqueID() {
        return "TEX_" + getCount();
    }

    /*
     * Returns the string ID of the texture. Useful for internal textures
     * 
     * @Returns: string ID on succes, null on failure
     */
    public String loadFromFile(final URL file) {
        final String id = generateUniqueID();

        if (loadFromURL(file, id)) {
            return id;
        }

        return null;
    }

    /*
     * Loads a texture from a file and links it with the given ID
     */
    public boolean loadFromURL(final URL file, final String textureID) {

        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage argbImg = new BufferedImage(img.getWidth(), img
                    .getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = argbImg.createGraphics();
            g.drawImage(img, 0, 0, null);
            Texture texture = TextureIO.newTexture(img, false);
            return put(textureID, texture);

        } catch (final IOException e) {
            LOG.error("IO exception durng loading of " + file, e);
        } catch (final GLException e) {
            LOG.error("GL exception durng loading of " + file, e);
        }

        return false;
    }

    public void loadFromTextureData(final String name, final TextureData texData) {
        final Texture tex = TextureIO.newTexture(texData);
        put(name, tex);
    }
}
