package walledin.engine;

import java.io.*;

public class Font {
	private String name; 

	public boolean readFromFile(String filename) {
		try {
			DataInputStream in = new DataInputStream(
					new BufferedInputStream(new FileInputStream(filename)));

			try {
				final int nameLength = in.readInt();
				System.out.println(nameLength);
				
				final byte[] nameBuf = new byte[nameLength];
				in.read(nameBuf, 0, nameLength);	
				name = new String(nameBuf);
				System.out.println("Font name: " + name); // for debugging
				
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
