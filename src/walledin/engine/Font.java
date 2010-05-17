package walledin.engine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;

public class Font {

	public class Glyph {
		public int width;
		public int height;
		public int advance;
		public int bearingX;
		public int bearingY;
		public int startX;
		public int startY;
		public char charCode;
	}

	private String name;
	private int width;
	private int height;
	private int glyphCount;
	private Map<Character, Glyph> glyphs;

	public String getName() {
		return name;
	}

	/* Helper function */
	private int toBigEndian(int i) {
		return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
				+ ((i >> 24) & 0xff);
	}

	public boolean readFromFile(String filename) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(filename)));

			try {
				int nameLength = toBigEndian(in.readInt());

				final byte[] nameBuf = new byte[nameLength];
				in.read(nameBuf, 0, nameLength);
				name = new String(nameBuf);

				glyphCount = toBigEndian(in.readInt());
				glyphs = new HashMap<Character, Glyph>();

				for (int i = 0; i < glyphCount; i++) {
					Glyph gl = new Glyph();
					gl.width = toBigEndian(in.readInt());
					gl.height = toBigEndian(in.readInt());
					gl.advance = toBigEndian(in.readInt());
					gl.bearingX = toBigEndian(in.readInt());
					gl.bearingY = toBigEndian(in.readInt());
					gl.startX = toBigEndian(in.readInt());
					gl.startY = toBigEndian(in.readInt());

					// try to read an UTF-8 char
					byte[] charCode = new byte[4];
					in.read(charCode, 0, 4);
					String s = new String(charCode, "UTF-8");
					gl.charCode = s.charAt(0);

					glyphs.put(gl.charCode, gl);
				}

				// read texture information
				width = toBigEndian(in.readInt());
				height = toBigEndian(in.readInt());
				final byte[] texBufArray = new byte[width * height * 2];
				in.read(texBufArray, 0, width * height * 2);

				TextureData texData = new TextureData(GL.GL_RGBA, width,
						height, 0, GL.GL_LUMINANCE_ALPHA, GL.GL_UNSIGNED_BYTE,
						true, false, false, ByteBuffer.wrap(texBufArray), null); // needs
				// flusher?

				// for now, use font name
				TextureManager.getInstance().loadFromTextureData(name, texData);

				in.close();

				return true;
			}

			catch (IOException iox) {
				System.out.println("Problems reading " + filename);
				in.close();
				return false;
			}
		}

		catch (IOException iox) {
			System.out.println("IO Problems with " + filename);
			return false;
		}
	}

	/**
	 * Renders a character to the screen and transposes the current matrix with
	 * the glyph advance.
	 * 
	 * @param renderer
	 *            Current renderer
	 * @param c
	 *            Character to render
	 * @param pos
	 *            Position to render to
	 */
	public void renderChar(Renderer renderer, Character c, Vector2f pos) {
		if (!glyphs.containsKey(c))
			return;

		Glyph glyph = glyphs.get(c);
		Texture tex = TextureManager.getInstance().get(name);

		// FIXME: calculate true texture positions somewhere else
		renderer.drawRect(name, new Rectangle((glyph.startX + 0.500f)
				/ (float) tex.getWidth(), (glyph.startY + 0.500f)
				/ (float) tex.getHeight(),
				(glyph.width - 1.000f) / (float) tex.getWidth(), (glyph.height - 1.000f)
						/ (float) tex.getHeight()), new Rectangle(pos.getX()
				+ glyph.bearingX, pos.getY() - glyph.bearingY, glyph.width,
				glyph.height));

		renderer.translate(new Vector2f(glyph.advance, 0));
	}

	public void renderText(Renderer renderer, String text, Vector2f pos) {
		renderer.pushMatrix();

		for (int i = 0; i < text.length(); i++)
			renderChar(renderer, text.charAt(i), pos);

		renderer.popMatrix();
	}

}
