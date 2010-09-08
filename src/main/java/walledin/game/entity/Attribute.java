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

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Vector2f;
import walledin.game.Team;

/**
 * Attributes of entities. An entity can have some of these, according to its
 * function.
 * 
 * @author Ben Ruijl
 * 
 */
public enum Attribute {
    /** Position of a spatial entity. */
    POSITION(Vector2f.class, true),
    /** Velocity of a spatial entity. */
    VELOCITY(Vector2f.class, true),
    /** Width of the entity. */
    WIDTH(Integer.class, true),
    /** Height of the entity. */
    HEIGHT(Integer.class, true),
    /**
     * Map tiles. Tiles do not get sent over network, but will be read locally.
     */
    TILES(List.class, false),
    /** Width of a tile. This is also the height. */
    TILE_WIDTH(Float.class, true),
    /** Name of a map. */
    MAP_NAME(String.class, true),
    /** Spawn points of a map. */
    SPAWN_POINTS(List.class, false),
    /** Health of the entity. */
    HEALTH(Integer.class, true),
    /** Name of the player that controls the entity. */
    PLAYER_NAME(String.class, true),
    /** The team of the player entity. */
    PLAYER_TEAM(Team.class, false),
    /** Orientation angle. */
    ORIENTATION_ANGLE(Float.class, true),
    /** The active weapon of a player entity. */
    ACTIVE_WEAPON(Entity.class),
    /**
     * An invisible grenade launcher. Useful for keeping track of the grenades.
     */
    GRENADE_LAUNCHER(Entity.class),
    /** Keeps track if the entity is picked up. */
    PICKED_UP(Boolean.class),
    /** The position of the cursor. */
    CURSOR_POS(Vector2f.class),
    /** The actions a player is about to do. */
    PLAYER_ACTIONS(Set.class),
    /** Frame of the player walking animation. */
    WALK_ANIM_FRAME(Float.class),
    /** Bounding geometry of a spatial entity. */
    BOUNDING_GEOMETRY(AbstractGeometry.class),
    /** Z-index of a visible entity. */
    Z_INDEX(Integer.class),
    /** Mass of the particle. */
    MASS(Float.class),
    /** Keeps track if the entity must not collide with other entities. */
    NO_COLLIDE(Boolean.class),
    /** Target position of a bullet particle or something else. */
    TARGET(Vector2f.class),
    /** Counter between zero and one that takes track of how blocked you are. */
    WALLEDIN_IN(Float.class, true),
    /**
     * The entity that did the last damage. If it is a bullet, the player that
     * shot the bullet is the last one to do damage.
     */
    LAST_DAMAGE(String.class, true),
    /**
     * The entity that owns this entity. A bullet is owned by the player that
     * shoots it.
     */
    OWNED_BY(String.class, true);

    /** Class of the attribute. */
    private final Class<?> clazz;
    /** Checks if it can be sent over network. */
    private final boolean sendOverNetwork;

    /**
     * Creates a new attribute that cannot be sent over network.
     * 
     * @param clazz
     *            Class of the attribute.
     */
    private Attribute(final Class<?> clazz) {
        this(clazz, false);
    }

    /**
     * Creates a new attribute.
     * 
     * @param clazz
     *            Class of the attribute
     * @param sendOverNetwork
     *            True if it can be sent over network, else false
     */
    private Attribute(final Class<?> clazz, final boolean sendOverNetwork) {
        this.clazz = clazz;
        this.sendOverNetwork = sendOverNetwork;
    }

    /**
     * Returns if the entity can be sent over network.
     * 
     * @return See above.
     */
    public boolean canSendOverNetwork() {
        return sendOverNetwork;
    }

    public Class<?> getClazz() {
        return clazz;
    }

}
