/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

*/
package walledin.game.entity.behaviors;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;

public class MapRenderBehavior extends RenderBehavior {
	private final static float TILE_WIDTH = 32.0f;
	private final static int STEP_SIZE = 10;
	private int height;
	private int width;
	private List<Tile> tiles;

	public MapRenderBehavior(final Entity owner) {
		super(owner, ZValues.MAP);
		setAttribute(Attribute.RENDER_TILE_SIZE, TILE_WIDTH);
		tiles = new ArrayList<Tile>();
	}

	private void render(final Renderer renderer) {
		/* Partition the map */
		for (int sw = 0; sw < width; sw += STEP_SIZE) {
			for (int sh = 0; sh < height; sh += STEP_SIZE) {
				renderPart(renderer, sw, sh);

			}
		}
	}

	private void renderPart(final Renderer renderer, int sw, int sh) {
		Rectangle part = new Rectangle(sw * TILE_WIDTH, sh * TILE_WIDTH,
				TILE_WIDTH * STEP_SIZE, TILE_WIDTH * STEP_SIZE);
		if (renderer.inFrustum(part)) {
			for (int i = 0; i < Math.min(STEP_SIZE, height - sh); i++) {
				for (int j = 0; j < Math.min(STEP_SIZE, width - sw); j++) {
					int index = (sh + i) * width + sw + j;
					if (index > 0 && index < tiles.size()) {
						final Tile tile = tiles.get(index);
						renderer.drawTexturePart(tile.getType()
								.getTexturePartID(), new Rectangle((sw + j)
								* TILE_WIDTH, (sh + i) * TILE_WIDTH,
								TILE_WIDTH, TILE_WIDTH));
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
