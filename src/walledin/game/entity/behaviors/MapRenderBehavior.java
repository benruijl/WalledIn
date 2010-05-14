package walledin.game.entity.behaviors;

import java.util.List;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;

public class MapRenderBehavior extends RenderBehavior {
	final float tileWidth = 32.0f;
	final int stepSize = 10;
	final int height;
	final int width;
	final List<Tile> tiles;

	public MapRenderBehavior(final Entity owner, final int width,
			final int height, final List<Tile> tiles) {
		super(owner, ZValues.MAP);
		this.width = width;
		this.height = height;
		this.tiles = tiles;
		
		setAttribute(Attribute.RENDER_TILE_SIZE, tileWidth);
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
							final Tile tile = tiles.get((sh + i) * width + sw
									+ j);
							renderer.drawTexturePart(tile.getType()
									.getTexturePartID(), new Rectangle((sw + j)
									* tileWidth, (sh + i) * tileWidth,
									tileWidth, tileWidth));
						}
					}
				}

			}
		}
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.RENDER) {
			render((Renderer) data);
		}

	}

}
