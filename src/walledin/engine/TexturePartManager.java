package walledin.engine;

import java.util.logging.Logger;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

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

	public boolean createTexturePart(String texturePartID, String textureID,
			Rectangle rectangle) {
		Texture texture = TextureManager.getInstance().get(textureID);
		float width = (float) texture.getWidth();
		float height = (float) texture.getHeight();
		Vector2f scale = new Vector2f(1 / width, 1 / height);
		Rectangle scaledRectangle = rectangle.scaleAll(scale);
		TexturePart part = new TexturePart(texture, scaledRectangle);
		return put(texturePartID, part);
	}
}