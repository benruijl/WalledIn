package walledin.game.entity.behaviors;

import java.util.List;

import walledin.engine.Renderer;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;
import walledin.math.Rectangle;

public class MapRenderBehavior extends RenderBehavior {
	private final static float TILE_WIDTH = 32.0f;
	private final static int STEP_SIZE = 10;
	private int height;
	private int width;
	private List<Tile> tiles;

	public MapRenderBehavior(final Entity owner) {
		super(owner, ZValues.MAP);
		setAttribute(Attribute.RENDER_TILE_SIZE, TILE_WIDTH);
	}

	private void render(final Renderer renderer) {
		/* Partition the map */
		for (int sw = 0; sw < width; sw += STEP_SIZE) {
			for (int sh = 0; sh < height; sh += STEP_SIZE) {
				if (renderer.inFrustum(new Rectangle(sw * TILE_WIDTH, sh
						* TILE_WIDTH, TILE_WIDTH * STEP_SIZE, TILE_WIDTH
						* STEP_SIZE))) {
					for (int i = 0; i < Math.min(STEP_SIZE, height - sh); i++) {
						for (int j = 0; j < Math.min(STEP_SIZE, width - sw); j++) {
							final Tile tile = tiles.get((sh + i) * width + sw
									+ j);
							renderer.drawTexturePart(tile.getType()
									.getTexturePartID(), new Rectangle((sw + j)
									* TILE_WIDTH, (sh + i) * TILE_WIDTH,
									TILE_WIDTH, TILE_WIDTH));
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
		} else if (messageType == MessageType.ATTRIBUTE_SET) {
			final Attribute attribute = (Attribute) data;
			switch (attribute) {
			case HEIGHT:
				height = getAttribute(attribute);
				break;
			case WIDTH:
				width = getAttribute(attribute);
				break;
			case TILES:
				tiles = getAttribute(attribute);
				break;
			}
		}
	}
}
