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
package walledin.game;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.math.Geometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;

/**
 * CollisionManager checks for collisions between all non-map entities and
 * between entities and the map.
 * 
 * @author Ben Ruijl
 * 
 */
public class CollisionManager {
    private static final Logger LOG = Logger.getLogger(CollisionManager.class);

    public static class CollisionData {
        private final Vector2f newPos;
        private final Vector2f oldPos;
        private final Vector2f theorPos;
        private final double delta;
        private final Entity collisionEntity;

        /**
         * @param newPos
         *            The position after the collision detection
         * @param oldPos
         *            The position before the velocity update
         * @param theorPos
         *            The theoretical position after the velocity update but
         *            before the collision check
         * @param delta 
         *            Delta time
         * @param collisionEntity
         *            The entity the entity that receives the message collided
         *            with
         */
        public CollisionData(final Vector2f newPos, final Vector2f oldPos,
                final Vector2f theorPos, final double delta, final Entity collisionEntity) {
            super();
            this.newPos = newPos;
            this.oldPos = oldPos;
            this.theorPos = theorPos;
            this.delta = delta;
            this.collisionEntity = collisionEntity;
        }

        public final Vector2f getNewPos() {
            return newPos;
        }

        public final Vector2f getOldPos() {
            return oldPos;
        }

        public final Vector2f getTheorPos() {
            return theorPos;
        }
        
        public final double getDelta() {
            return delta;
        }

        public final Entity getCollisionEntity() {
            return collisionEntity;
        }

    }

    /**
     * Calculates the tile containing the point <code>pos</code>.
     * 
     * @param map
     *            Map
     * @param pos
     *            The position
     * @return The tile containing <code>pos</code>.
     */
    private static Tile tileFromPixel(final Entity map, final Vector2f pos) {
        final float tileSize = (Float) map
                .getAttribute(Attribute.RENDER_TILE_SIZE);
        final int width = (Integer) map.getAttribute(Attribute.WIDTH);
        final int height = (Integer) map.getAttribute(Attribute.HEIGHT);
        final List<Tile> tiles = (List<Tile>) map.getAttribute(Attribute.TILES);
        final int x = (int) (pos.getX() / tileSize);
        final int y = (int) (pos.getY() / tileSize);

        if (x < 0 || x < 0 || x >= width || y >= height) {
            LOG.fatal("Illegal tile requested! "
                    + "Tried to access tile at coordinates (" + x + "," + y
                    + "), but the boundaries are (0,0) - (" + width + ","
                    + height + ").");
        }

        return tiles.get((int) (pos.getX() / tileSize) + width
                * (int) (pos.getY() / tileSize));
    }

    /**
     * Performs collision detection between entities other than a map.
     * 
     * @param entities
     *            Entities to check
     * @param delta
     *            Delta time, used for interpolation
     */
    public static void calculateEntityCollisions(
            final Collection<Entity> entities, final double delta) {
        Entity[] entArray = new Entity[0];
        entArray = entities.toArray(entArray);

        for (int i = 0; i < entArray.length - 1; i++) {
            for (int j = i + 1; j < entArray.length; j++) {
                if (!entArray[i].hasAttribute(Attribute.BOUNDING_GEOMETRY)
                        || !entArray[j]
                                .hasAttribute(Attribute.BOUNDING_GEOMETRY)) {
                    continue;
                }

                if (entArray[j].getAttribute(Attribute.VELOCITY).equals(
                        new Vector2f(0, 0))
                        && entArray[i].getAttribute(Attribute.VELOCITY).equals(
                                new Vector2f(0, 0))) {
                    continue;
                }

                Geometry boundsA = (Geometry) entArray[i]
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);
                Geometry boundsB = (Geometry) entArray[j]
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);

                boundsA = boundsA.translate((Vector2f) entArray[i]
                        .getAttribute(Attribute.POSITION));
                boundsB = boundsB.translate((Vector2f) entArray[j]
                        .getAttribute(Attribute.POSITION));

                if (!boundsA.intersects(boundsB)) {
                    continue;
                }

                final Vector2f posA = (Vector2f) entArray[i]
                        .getAttribute(Attribute.POSITION);
                final Vector2f posB = (Vector2f) entArray[j]
                        .getAttribute(Attribute.POSITION);

                final Vector2f velA = ((Vector2f) entArray[i]
                        .getAttribute(Attribute.VELOCITY)).scale((float) delta);
                final Vector2f velB = ((Vector2f) entArray[j]
                        .getAttribute(Attribute.VELOCITY)).scale((float) delta);

                final Vector2f oldPosA = posA.sub(velA);
                final Vector2f oldPosB = posB.sub(velB);

                entArray[i].sendMessage(MessageType.COLLIDED,
                        new CollisionData(posA, oldPosA, posA, delta, entArray[j]));
                entArray[j].sendMessage(MessageType.COLLIDED,
                        new CollisionData(posB, oldPosB, posB, delta, entArray[i]));
            }
        }
    }

    /**
     * Performs collision detection between entities and the map.
     * 
     * @param map
     *            The map to check against
     * @param entities
     *            Entities in the map
     * @param delta
     *            Delta time, used for interpolation
     */
    public static void calculateMapCollisions(final Entity map,
            final Collection<Entity> entities, final double delta) {
        final float tileSize = (Float) map
                .getAttribute(Attribute.RENDER_TILE_SIZE);

        for (final Entity ent : entities) {
            if (ent.hasAttribute(Attribute.BOUNDING_GEOMETRY)
                    && !ent.equals(map)) {

                Vector2f vel = (Vector2f) ent.getAttribute(Attribute.VELOCITY);

                // skip static entities
                if (vel.equals(new Vector2f(0, 0))) {
                    continue;
                }

                vel = vel.scale((float) delta); // velocity per frame
                final Geometry bounds = (Geometry) ent
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);
                Rectangle rect = bounds.asRectangle();
                final Vector2f curPos = (Vector2f) ent
                        .getAttribute(Attribute.POSITION);
                final Vector2f oldPos = curPos.sub(vel);

                float x = curPos.getX(); // new x position after collision
                float y = curPos.getY(); // new y position after collision

                // small value to prevent floating errors
                final float eps = 0.001f;

                // VERTICAL CHECK - move vertically only
                rect = rect.setPos(new Vector2f(oldPos.getX(), curPos.getY()));

                // check the four edges
                Tile lt = tileFromPixel(map, rect.getLeftTop());
                Tile lb = tileFromPixel(map, rect.getLeftBottom());
                Tile rt = tileFromPixel(map, rect.getRightTop());
                Tile rb = tileFromPixel(map, rect.getRightBottom());

                // bottom check
                if (vel.getY() > 0
                        && (lb.getType().isSolid() || rb.getType().isSolid())) {
                    final int rest = (int) (rect.getBottom() / tileSize);
                    y = rest * tileSize - rect.getHeight() - eps;
                } else
                // top check
                if (vel.getY() < 0
                        && (lt.getType().isSolid() || rt.getType().isSolid())) {
                    final int rest = (int) (rect.getTop() / tileSize);
                    y = (rest + 1) * tileSize + eps;
                }

                // HORIZONTAL CHECK - move horizontally only
                rect = rect.setPos(new Vector2f(curPos.getX(), y));

                lt = tileFromPixel(map, rect.getLeftTop());
                lb = tileFromPixel(map, rect.getLeftBottom());
                rt = tileFromPixel(map, rect.getRightTop());
                rb = tileFromPixel(map, rect.getRightBottom());

                // right check
                if (vel.getX() > 0
                        && (rt.getType().isSolid() || rb.getType().isSolid())) {
                    final int rest = (int) (rect.getRight() / tileSize);
                    x = rest * tileSize - rect.getWidth() - eps;
                } else
                // left check
                if (vel.getX() < 0
                        && (lt.getType().isSolid() || lb.getType().isSolid())) {
                    final int rest = (int) (rect.getLeft() / tileSize);
                    x = (rest + 1) * tileSize + eps;
                }

                ent.setAttribute(Attribute.POSITION, new Vector2f(x, y));
                ent.setAttribute(Attribute.VELOCITY,
                        new Vector2f(x - oldPos.getX(), y - oldPos.getY())
                                .scale((float) (1 / delta)));

                // if there is no difference, there has been no collision
                if (Math.abs(x - curPos.getX()) > eps
                        || Math.abs(y - curPos.getY()) > eps) {
                    ent.sendMessage(MessageType.COLLIDED, new CollisionData(
                            new Vector2f(x, y), oldPos, curPos, delta, map));
                }
            }
        }

    }

}
