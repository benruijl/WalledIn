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

import walledin.engine.math.Geometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;

public class CollisionManager {

    public static class CollisionData {
        private final Vector2f newPos;
        private final Vector2f oldPos;
        private final Vector2f theorPos;
        private final Entity collisionEntity;

        /**
         * @param newPos
         *            The position after the collision detection
         * @param oldPos
         *            The position before the velocity update
         * @param theorPos
         *            The theoretical position after the velocity update but
         *            before the collision check
         * @param collisionEntity
         *            The entity the entity that receives the message collided
         *            with
         */
        public CollisionData(final Vector2f newPos, final Vector2f oldPos,
                final Vector2f theorPos, final Entity collisionEntity) {
            super();
            this.newPos = newPos;
            this.oldPos = oldPos;
            this.theorPos = theorPos;
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

        public final Entity getCollisionEntity() {
            return collisionEntity;
        }

    }

    private static Tile tileFromPixel(final Entity map, final Vector2f pos) {
        final float tileSize = (Float) map
                .getAttribute(Attribute.RENDER_TILE_SIZE);
        final int width = (Integer) map.getAttribute(Attribute.WIDTH);

        final List<Tile> tiles = (List<Tile>) map.getAttribute(Attribute.TILES);

        return tiles.get((int) (pos.x / tileSize) + width
                * (int) (pos.y / tileSize));
    }

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

                Geometry boundsA = (Geometry) entArray[i]
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);
                Geometry rectB = (Geometry) entArray[j]
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);

                boundsA = boundsA.translate((Vector2f) entArray[i]
                        .getAttribute(Attribute.POSITION));
                rectB = rectB.translate((Vector2f) entArray[j]
                        .getAttribute(Attribute.POSITION));

                if (!boundsA.intersects(rectB)) {
                    continue;
                }

                final Vector2f posA = (Vector2f) entArray[i]
                        .getAttribute(Attribute.POSITION);
                final Vector2f posB = (Vector2f) entArray[j]
                        .getAttribute(Attribute.POSITION);

                // no response yet, so give the same data
                entArray[i].sendMessage(MessageType.COLLIDED,
                        new CollisionData(posA, posA, posA, entArray[j]));
                entArray[j].sendMessage(MessageType.COLLIDED,
                        new CollisionData(posB, posB, posB, entArray[i]));
            }
        }
    }

    public static void calculateMapCollisions(final Entity map,
            final Collection<Entity> entities, final double delta) {
        final float tileSize = (Float) map
                .getAttribute(Attribute.RENDER_TILE_SIZE);

        for (final Entity ent : entities) {
            if (ent.hasAttribute(Attribute.BOUNDING_GEOMETRY)
                    && !ent.equals(map)) {

                Vector2f vel = (Vector2f) ent.getAttribute(Attribute.VELOCITY);

                if (vel.x == 0 && vel.y == 0) {
                    continue;
                }

                vel = vel.scale((float) delta); // velocity per frame
                final Geometry bounds = (Geometry) ent
                        .getAttribute(Attribute.BOUNDING_GEOMETRY);
                Rectangle rect = bounds.asRectangle();
                final Vector2f curPos = (Vector2f) ent
                        .getAttribute(Attribute.POSITION);
                final Vector2f oldPos = curPos.sub(vel);

                float x = curPos.x; // new x position after collision
                float y = curPos.y; // new y position after collision

                // small value to prevent floating errors
                final float eps = 0.001f;

                // VERTICAL CHECK - move vertically only
                rect = rect.setPos(new Vector2f(oldPos.x, curPos.y));

                // check the four edges
                Tile lt = tileFromPixel(map, rect.getLeftTop());
                Tile lb = tileFromPixel(map, rect.getLeftBottom());
                Tile rt = tileFromPixel(map, rect.getRightTop());
                Tile rb = tileFromPixel(map, rect.getRightBottom());

                // bottom check
                if (vel.y > 0
                        && (lb.getType().isSolid() || rb.getType().isSolid())) {
                    final int rest = (int) (rect.getBottom() / tileSize);
                    y = rest * tileSize - rect.getHeight() - eps;
                } else
                // top check
                if (vel.y < 0
                        && (lt.getType().isSolid() || rt.getType().isSolid())) {
                    final int rest = (int) (rect.getTop() / tileSize);
                    y = (rest + 1) * tileSize + eps;
                }

                // HORIZONTAL CHECK - move horizontally only
                rect = rect.setPos(new Vector2f(curPos.x, y));

                lt = tileFromPixel(map, rect.getLeftTop());
                lb = tileFromPixel(map, rect.getLeftBottom());
                rt = tileFromPixel(map, rect.getRightTop());
                rb = tileFromPixel(map, rect.getRightBottom());

                // right check
                if (vel.x > 0
                        && (rt.getType().isSolid() || rb.getType().isSolid())) {
                    final int rest = (int) (rect.getRight() / tileSize);
                    x = rest * tileSize - rect.getWidth() - eps;
                } else
                // left check
                if (vel.x < 0
                        && (lt.getType().isSolid() || lb.getType().isSolid())) {
                    final int rest = (int) (rect.getLeft() / tileSize);
                    x = (rest + 1) * tileSize + eps;
                }

                ent.setAttribute(Attribute.POSITION, new Vector2f(x, y));
                ent.setAttribute(Attribute.VELOCITY, new Vector2f(x - oldPos.x,
                        y - oldPos.y).scale((float) (1 / delta)));

                // if there is no difference, there has been no collision
                if (Math.abs(x - curPos.x) > 0.0001f
                        || Math.abs(y - curPos.y) > 0.0001f) {
                    ent.sendMessage(MessageType.COLLIDED, new CollisionData(
                            new Vector2f(x, y), oldPos, curPos, map));
                }
            }
        }

    }

}
