package walledin.engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 * 
 * @author ben
 */
public class TextureManager extends ResourceManager<String, Texture> {
	private final static Logger LOG = Logger.getLogger(TextureManager.class
			.getName());
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
	public String loadFromFile(final String filename) {
		final String id = generateUniqueID();

		if (loadFromFile(filename, id)) {
			return id;
		}

		return null;
	}

	/*
	 * Loads a texture from a file and links it with the given ID
	 */
	public boolean loadFromFile(final String filename, final String textureID) {
		try {
			final Texture texture = TextureIO.newTexture(new File(filename),
					true);
			return put(textureID, texture);

		} catch (final IOException ex) {
			LOG.log(Level.SEVERE, null, ex);
		} catch (final GLException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}

		return false;
	}

	public void loadFromTextureData(String name, TextureData texData) {
		Texture tex = TextureIO.newTexture(texData);
		put(name, tex);
	}
}
