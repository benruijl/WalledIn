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
package walledin.game.entity.behaviors.render;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.game.ZValues;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;

/**
 * This class renders the map. It optimizes drawing by dividing the map into
 * blocks and checking if the blocks are in the frustum.
 * 
 * @author Ben Ruijl
 */
public class MapRenderBehavior extends RenderBehavior {
    /** The width in tiles of the block that should be rendered. */
    private final int stepSize = 10;
    /** The width of a tile. */
    private float tileWidth;
    /** Map height in tiles. */
    private int height;
    /** Map width in tiles. */
    private int width;
    /** List of map tiles. */
    private List<Tile> tiles;

    /**
     * Creates a new map render behavior.
     * 
     * @param owner
     *            Map entity
     */
    public MapRenderBehavior(final Entity owner) {
        super(owner, ZValues.MAP);
        tiles = new ArrayList<Tile>();
    }

    /**
     * Renders the map.
     * 
     * @param renderer
     *            Renderer
     */
    private void render(final Renderer renderer) {
        /* Partition the map */
        for (int sw = 0; sw < width; sw += stepSize) {
            for (int sh = 0; sh < height; sh += stepSize) {
                renderPart(renderer, sw, sh);

            }
        }
    }

    /**
     * Renders a sub-rectangle of the map. Useful for improving speed.
     * 
     * @param renderer
     *            Renderer
     * @param sw
     *            X-coordinate of the top left tile
     * @param sh
     *            Y-coordinate of the top left tile
     */
    private void renderPart(final Renderer renderer, final int sw, final int sh) {
        final Rectangle part = new Rectangle(sw * tileWidth, sh * tileWidth,
                tileWidth * stepSize, tileWidth * stepSize);
        if (renderer.inFrustum(part)) {
            for (int i = 0; i < Math.min(stepSize, height - sh); i++) {
                for (int j = 0; j < Math.min(stepSize, width - sw); j++) {
                    final int index = (sh + i) * width + sw + j;
                    if (index >= 0 && index < tiles.size()) {
                        final Tile tile = tiles.get(index);
                        renderer.drawTexturePart(tile.getType()
                                .getTexturePartID(), new Rectangle((sw + j)
                                * tileWidth, (sh + i) * tileWidth, tileWidth,
                                tileWidth));
                    }
                }
            }
        }
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.RENDER) {
            render((Renderer) data);
        } else if (messageType == MessageType.ATTRIBUTE_SET) {
            final Attribute attribute = (Attribute) data;
            switch (attribute) {
            case HEIGHT:
                height = (Integer) getAttribute(attribute);
                break;
            case WIDTH:
                width = (Integer) getAttribute(attribute);
                break;
            case TILES:
                tiles = (List<Tile>) getAttribute(attribute);
                break;
            case TILE_WIDTH:
                tileWidth = (Float) getAttribute(attribute);
                break;
            default:
                break;
            }
        }
    }
}
