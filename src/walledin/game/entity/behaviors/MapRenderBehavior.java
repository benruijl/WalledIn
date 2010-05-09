package walledin.game.entity.behaviors;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.Tile;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class MapRenderBehavior extends RenderBehavior {
	final private String texture;
	final float tileWidth = 32.0f;
	final int stepSize = 10;
	final int height;
	final int width;
	final Tile[] tiles;

	public MapRenderBehavior(Entity owner, String texture, int width,
			int height, Tile[] tiles) {
		super(owner);

		this.texture = texture;
		this.width = width;
		this.height = height;
		this.tiles = tiles;
	}

	private void render(final Renderer renderer) {
		/* Partition the map */
		for (int sw = 0; sw < width; sw += stepSize) {
			for (int sh = 0; sh < height; sh += stepSize) {
				if (renderer
						.inFrustum(new Rectangle(sw * tileWidth,
								sh * tileWidth, tileWidth * stepSize, tileWidth
										* stepSize))) {
					for (int i = 0; i < Math.min(stepSize, height - sh); i++) {
						for (int j = 0; j < Math.min(stepSize, width - sw); j++) {
							final Tile tile = tiles[(sh + i) * width + sw + j];

							renderer.drawRect(texture, tile.getTexRect(),
									new Rectangle((sw + j) * tileWidth,
											(sh + i) * tileWidth, tileWidth,
											tileWidth));
						}
					}
				}

			}
		}
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.RENDER)
			render((Renderer) data);

	}

}
