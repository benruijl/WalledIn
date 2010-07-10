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

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import org.apache.log4j.Logger;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;

public class Font {

    /**
     * A glyph is the visual representation of a single character in a font. It
     * can be identified by its corresponding char code.
     * 
     * @author Ben Ruijl
     * 
     */
    private static class Glyph {
        private final int width;
        private final int height;
        private final int advance;
        private final int bearingX;
        private final int bearingY;
        private final int startX;
        private final int startY;
        private final char charCode;

        public Glyph(final DataInputStream in) throws IOException {
            width = toBigEndian(in.readInt());
            height = toBigEndian(in.readInt());
            advance = toBigEndian(in.readInt());
            bearingX = toBigEndian(in.readInt());
            bearingY = toBigEndian(in.readInt());
            startX = toBigEndian(in.readInt());
            startY = toBigEndian(in.readInt());

            // try to read an UTF-8 char
            final byte[] charCodeArray = new byte[4];
            in.read(charCodeArray, 0, 4);
            final String s = new String(charCodeArray, "UTF-8");
            charCode = s.charAt(0);
        }
    }

    private static final Logger LOG = Logger.getLogger(Font.class);
    private String name;
    private int texWidth;
    private int texHeight;
    private int glyphCount;
    private Map<Character, Glyph> glyphs;

    /**
     * Get font name.
     * 
     * @return Font name
     */
    public String getName() {
        return name;
    }

    /**
     * Converts a little endian int to big endian int.
     * 
     * @param i
     *            little endian int
     * @return big endian int
     */
    private static int toBigEndian(final int i) {

        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
                + (i >> 24 & 0xff);
    }

    /**
     * Reads a font from a custom format.
     * 
     * @param file
     *            Font file. Custom .font format
     * @return True on success, false on failure
     */
    public final boolean readFromStream(final URL file) {
        DataInputStream in;
        try {
            in = new DataInputStream(file.openStream());
        } catch (final IOException e) {
            LOG.error("Could not open file: " + file, e);
            return false;
        }

        try {
            final int nameLength = toBigEndian(in.readInt());

            final byte[] nameBuf = new byte[nameLength];
            in.read(nameBuf, 0, nameLength);
            name = new String(nameBuf);

            glyphCount = toBigEndian(in.readInt());
            glyphs = new HashMap<Character, Glyph>();

            for (int i = 0; i < glyphCount; i++) {
                final Glyph gl = new Glyph(in);
                glyphs.put(gl.charCode, gl);
            }

            // read texture information
            texWidth = toBigEndian(in.readInt());
            texHeight = toBigEndian(in.readInt());
            final byte[] texBufArray = new byte[texWidth * texHeight * 2];
            // FIXME: ignores the amount of lines read
            in.read(texBufArray, 0, texWidth * texHeight * 2);

            final TextureData texData = new TextureData(GL.GL_RGBA, texWidth,
                    texHeight, 0, GL.GL_LUMINANCE_ALPHA, GL.GL_UNSIGNED_BYTE,
                    true, false, false, ByteBuffer.wrap(texBufArray), null); // needs
            // flusher?

            // for now, use font name
            TextureManager.getInstance().loadFromTextureData(name, texData);
        }

        catch (final IOException e) {
            LOG.error("Problems reading " + file, e);
            return false;
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                LOG.error("Could not close" + file, e);
            }
        }
        return true;
    }

    /**
     * Renders a character to the screen and transposes the current matrix with
     * the glyph's advance.
     * 
     * @param renderer
     *            Current renderer
     * @param c
     *            Character to render
     * @param pos
     *            Position to render to
     */
    public final void renderChar(final Renderer renderer, final Character c,
            final Vector2f pos) {
        if (!glyphs.containsKey(c)) {
            return;
        }

        final Glyph glyph = glyphs.get(c);
        final Texture tex = TextureManager.getInstance().get(name);

        // FIXME: calculate true texture positions somewhere else
        renderer.drawRect(name, new Rectangle((glyph.startX + 0.500f)
                / tex.getWidth(), (glyph.startY + 0.500f) / tex.getHeight(),
                (glyph.width - 1.000f) / tex.getWidth(),
                (glyph.height - 1.000f) / tex.getHeight()), new Rectangle(pos
                .getX()

                + glyph.bearingX, pos.getY() - glyph.bearingY, glyph.width,
                glyph.height));

        renderer.translate(new Vector2f(glyph.advance, 0));
    }

    /**
     * Renders text to the screen.
     * 
     * @param renderer
     *            Current renderer
     * @param text
     *            Unicode text to render
     * @param pos
     *            Position to render to
     */
    public final void renderText(final Renderer renderer, final String text,
            final Vector2f pos) {
        renderer.pushMatrix();

        for (int i = 0; i < text.length(); i++) {
            renderChar(renderer, text.charAt(i), pos);
        }

        renderer.popMatrix();
    }

    /**
     * Gets the width of an untransformed text.
     * 
     * @param text
     *            Text
     * @return Width
     */
    public final int getTextWidth(final String text) {
        int width = 0;

        for (int i = 0; i < text.length(); i++) {
            width += glyphs.get(text.charAt(i)).advance;
        }

        return width;
    }

    /**
     * Returns the height of the text.
     * 
     * @param text
     *            Text
     * @return Height of largest character
     */
    public final int getTextHeight(final String text) {
        int height = 0;

        for (int i = 0; i < text.length(); i++) {
            // TODO: check if height has to be corrected with bearing
            height = Math.max(glyphs.get(text.charAt(i)).height, height);
        }

        return height;
    }

    /**
     * Returns a bounding rectangle of the text.
     * 
     * @param text
     *            Text
     * @return Bounding rectangle
     */
    public final Rectangle getBoundingRect(final String text) {
        // find starting Y
        int bearingY = 0;
        for (int i = 0; i < text.length(); i++) {
            bearingY = Math.max(glyphs.get(text.charAt(i)).bearingY, bearingY);
        }

        // TODO: check if correct
        return new Rectangle(0, -bearingY, getTextWidth(text),
                getTextHeight(text));
    }

}
