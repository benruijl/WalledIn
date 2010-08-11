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
package walledin.game.entity;

import java.util.List;
import java.util.Set;

import walledin.engine.math.Geometry;
import walledin.engine.math.Vector2f;
import walledin.game.Teams;

public enum Attribute {
    POSITION(Vector2f.class, true),
    VELOCITY(Vector2f.class, true),
    WIDTH(Integer.class, true),
    HEIGHT(Integer.class, true),
    /**
     * Map tiles. Tiles do not get sent over network, but will be read locally.
     */
    TILES(List.class, false),
    TILE_WIDTH(Float.class, true),
    /** Map name will be sent separately at entity creation. */
    MAP_NAME(String.class, false),
    /** Spawn points of a map. */
    SPAWN_POINTS(List.class, false),
    HEALTH(Integer.class, true),
    PLAYER_NAME(String.class, true),
    PLAYER_TEAM(Teams.class, true),
    /** Orientation angle. */
    ORIENTATION_ANGLE(Float.class, true),
    ACTIVE_WEAPON(Entity.class),
    PICKED_UP(Boolean.class),
    CURSOR_POS(Vector2f.class),
    PLAYER_ACTIONS(Set.class),
    WALK_ANIM_FRAME(Float.class),
    BOUNDING_GEOMETRY(Geometry.class),
    Z_INDEX(Integer.class),
    MASS(Float.class),
    TARGET(Vector2f.class),
    /** Counter between zero and one that takes track of how blocked you are */
    WALLEDIN_IN(Float.class, true);

    public final Class<?> clazz;
    public final boolean sendOverNetwork;

    private Attribute(final Class<?> clazz) {
        this(clazz, false);
    }

    private Attribute(final Class<?> clazz, final boolean sendOverNetwork) {
        this.clazz = clazz;
        this.sendOverNetwork = sendOverNetwork;
    }
}
