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
package walledin.game.map;

import java.util.HashMap;
import java.util.Map;

public enum TileType {
    TILE_EMPTY(' ', false),
    TILE_FILLED('#', true),
    TILE_TOP_GRASS_END_LEFT('C', true),
    TILE_TOP_GRASS_END_RIGHT('D', true),
    TILE_TOP_GRASS('E', true),
    TILE_LEFT_GRASS('F', true),
    TILE_LEFT_MUD('G', true),
    TILE_RIGHT_MUD('H', true),
    TILE_TOP_LEFT_GRASS('I', true),
    TILE_BOTTOM_LEFT_MUD('J', true),
    TILE_BOTTOM_RIGHT_MUD('K', true),
    TILE_TOP_LEFT_GRASS_END('L', true),
    TILE_BOTTOM_MUD('M', true);

    private static final Map<Character, TileType> MAPPING = new HashMap<Character, TileType>();
    private final char mapChar;
    private final boolean solid;

    static {
        for (final TileType tile : values()) {
            MAPPING.put(tile.mapChar, tile);
        }
    }

    private TileType(final char mapChar, final boolean solid) {
        this.mapChar = mapChar;
        this.solid = solid;
    }

    public static TileType getTile(final char mapChar) {
        return MAPPING.get(mapChar);
    }

    public String getTexturePartID() {
        return name().toLowerCase();
    }

    public boolean isSolid() {
        return solid;
    }
}
