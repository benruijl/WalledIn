package walledin.engine;

import java.util.logging.Logger;

import walledin.math.Rectangle;
import walledin.math.Vector2f;

import com.sun.opengl.util.texture.Texture;

/**
 * 
 * @author ben
 */
public class TexturePartManager extends ResourceManager<String, TexturePart> {
	private final static Logger LOG = Logger.getLogger(TexturePartManager.class
			.getName());
	private static final TexturePartManager INSTANCE = new TexturePartManager();

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static TexturePartManager getInstance() {
		return INSTANCE;
	}

	private TexturePartManager() {

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
