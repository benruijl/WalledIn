package walledin.engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * 
 * @author ben
 */
public class TextureManager extends ResourceManager<String, Texture> {
	private final static Logger LOG = Logger.getLogger(TextureManager.class
			.getName());

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static TextureManager getInstance() {
		if (ref == null) {
			ref = new TextureManager();
		}

		return ref;
	}

	private TextureManager() {

	}

	private static TextureManager ref = null;

	String generateUniqueID() {
		return "TEX_" + count().toString();
	}

	/*
	 * Returns the string ID of the texture. Useful for internal textures
	 * 
	 * @Returns: string ID on succes, null on failure
	 */
	public String LoadFromFile(final String strFilename) {
		final String id = generateUniqueID();

		if (LoadFromFile(strFilename, id)) {
			return id;
		}

		return null;
	}

	/*
	 * Loads a texture from a file and links it with the given ID
	 */
	public boolean LoadFromFile(final String strFilename, final String strTexID) {
		try {
			final Texture tex = TextureIO.newTexture(new File(strFilename),
					true);
			insert(strTexID, tex);
			return true;

		} catch (final IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		} catch (final GLException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}

		return false;
	}
}
