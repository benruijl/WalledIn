package walledin.engine;

import walledin.engine.math.Rectangle;

import com.sun.opengl.util.texture.Texture;

public class TexturePart {

	private final Texture texture;
	private final Rectangle rectangle;

	public TexturePart(Texture texture, Rectangle rectangle) {
		this.texture = texture;
		this.rectangle = rectangle;
	}

	public Texture getTexture() {
		return texture;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

}
