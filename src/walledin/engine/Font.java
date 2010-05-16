package walledin.engine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

public class Font {
	
	public class Glyph
	{
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
	
	private int toBigEndian(int i)
	{
		return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
	}
	
	public boolean readFromFile(String filename) {
		try {
			DataInputStream in = new DataInputStream(
					new BufferedInputStream(new FileInputStream(filename)));

			try {
				
				int nameLength = toBigEndian(in.readInt());
				System.out.println(nameLength);

				
				final byte[] nameBuf = new byte[nameLength];
				in.read(nameBuf, 0, nameLength);	
				name = new String(nameBuf);
				System.out.println("Font name: " + name); // for debugging
				
				glyphCount = toBigEndian(in.readInt());
				System.out.println(glyphCount);
				glyphs = new HashMap<Character, Glyph>();
				
				Charset charset = Charset.forName("UTF-8");
				CharsetDecoder decoder = charset.newDecoder();
				CharsetEncoder encoder = charset.newEncoder();
				
				for (int i = 0; i < glyphCount; i++)
				{
					Glyph gl = new Glyph();
					gl.width = toBigEndian(in.readInt());
					gl.height = toBigEndian(in.readInt());
					gl.advance = toBigEndian(in.readInt());
					gl.bearingX = toBigEndian(in.readInt());
					gl.bearingY = toBigEndian(in.readInt());
					gl.startX = toBigEndian(in.readInt());
					gl.startY = toBigEndian(in.readInt());
					
					// try to read an UTF-8 char, this is wrong
					ByteBuffer b = ByteBuffer.wrap(new byte[8]);
					b.asCharBuffer().append(in.readChar());
					b.asCharBuffer().append(in.readChar());
					b.asCharBuffer().append(in.readChar());
					b.asCharBuffer().append(in.readChar());
					gl.charCode = decoder.decode(b).get(0);					
					glyphs.put(gl.charCode, gl);
				}
				
				// read texture information
				width = toBigEndian(in.readInt());
				height = toBigEndian(in.readInt());
				final byte[] texBuf = new byte[width * height * 2];
				in.read(texBuf, 0, width * height * 2);
				
				
				System.out.println("Font read from file " +  filename + ".");
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

}
