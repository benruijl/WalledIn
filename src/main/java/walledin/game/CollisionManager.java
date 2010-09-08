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

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Circle;
import walledin.engine.math.Polygon2f;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;
import walledin.game.map.TileType;
import walledin.util.SettingsManager;

/**
 * CollisionManager checks for collisions between all non-map entities and
 * between entities and the map.
 * 
 * @author Ben Ruijl
 * 
 */
public final class CollisionManager {
    private static final Logger LOG = Logger.getLogger(CollisionManager.class);
    private static final float FLOOR_DAMPING = SettingsManager.getInstance()
            .getFloat("game.floorDamping");

    public static class GeometricalCollisionData {
        private final boolean collided;
        private final float time;
        private final Vector2f normal;
        private final Vector2f penetration;

        public GeometricalCollisionData(final boolean collided,
                final float time, final Vector2f normal,
                final Vector2f penetration) {
            super();
            this.collided = collided;
            this.time = time;
            this.normal = normal;
            this.penetration = penetration;
        }

        public boolean isCollided() {
            return collided;
        }

        public float getTime() {
            return time;
        }

        public Vector2f getNormal() {
            return normal;
        }

        public Vector2f getPenetration() {
            return penetration;
        }
    }

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
                final Vector2f theorPos, final double delta,
                final Entity collisionEntity) {
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
     * This class should not be created
     */
    private CollisionManager() {

    }

    /**
     * Calculates the tile containing the point <code>pos</code>. If the
     * coordinates are illegal, a solid tile is returned and a warning is given.
     * 
     * @param map
     *            Map
     * @param pos
     *            The position
     * @return The tile containing <code>pos</code>.
     */
    private static Tile tileFromPixel(final Entity map, final Vector2f pos) {
        final float tileSize = (Float) map.getAttribute(Attribute.TILE_WIDTH);
        final int width = (Integer) map.getAttribute(Attribute.WIDTH);
        final int height = (Integer) map.getAttribute(Attribute.HEIGHT);
        final List<Tile> tiles = (List<Tile>) map.getAttribute(Attribute.TILES);
        final int x = (int) (pos.getX() / tileSize);
        final int y = (int) (pos.getY() / tileSize);

        if (x < 0 || y < 0 || x >= width || y >= height) {
            LOG.warn("Illegal tile requested! "
                    + "Tried to access tile at coordinates (" + x + "," + y
                    + "), but the boundaries are (0,0) - (" + width + ","
                    + height + ").");

            return new Tile(TileType.TILE_FILLED, x, y);
        }

        return tiles.get((int) (pos.getX() / tileSize) + width
                * (int) (pos.getY() / tileSize));
    }

    /**
     * Resolved the collision between a moving polygon and a stationary circle.
     * 
     * @param polygonEntity
     *            The entity with a polygonal bounding geometry
     * @param circleEntity
     *            The entity with a circular bounding geometry
     * @param delta
     *            Delta time. Used for conversion of the velocity.
     * @return true if colliding, else false.
     */
    public static boolean resolvePolygonCircleCollision(
            final Entity polygonEntity, final Entity circleEntity,
            final double delta) {

        final Vector2f theoreticalPolygonPosition = (Vector2f) polygonEntity
                .getAttribute(Attribute.POSITION);

        final Vector2f circlePosition = (Vector2f) circleEntity
                .getAttribute(Attribute.POSITION);

        final Vector2f polygonVelocity = ((Vector2f) polygonEntity
                .getAttribute(Attribute.VELOCITY)).scale((float) delta);

        final Vector2f polygonOldPos = theoreticalPolygonPosition
                .sub(polygonVelocity);

        /* Polygon and circle at old position. */
        final Polygon2f polygon = ((AbstractGeometry) polygonEntity
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate(polygonOldPos).asPolygon();
        final Circle circle = ((AbstractGeometry) circleEntity
                .getAttribute(Attribute.BOUNDING_GEOMETRY))
                .asCircumscribedCircle().translate(circlePosition);

        /* The circle velocity is -PolygonVelocity. */
        final GeometricalCollisionData data = polygon.circleCollisionData(
                circle, polygonVelocity.scale(-1.0f));

        if (!data.isCollided()) {
            return false;
        }

        Vector2f newPolygonPos = polygonOldPos.add(data.getPenetration().scale(
                -1.0f));

        final float dn = polygonVelocity.dot(data.getNormal().scale(-1.0f));

        if (dn < 0) {
            newPolygonPos = newPolygonPos.add(polygonVelocity.scale(data
                    .getTime()));

            // slide
            Vector2f slideVel = polygonVelocity.scale(1.0f - data.getTime());
            slideVel = slideVel.sub(data.getNormal().scale(-1.0f)
                    .scale(slideVel.dot(data.getNormal().scale(-1.0f))));
            newPolygonPos = newPolygonPos.add(slideVel);
        } else {
            /* Moving away from collision. Allow. */
            newPolygonPos = newPolygonPos.add(polygonVelocity);
        }

        polygonEntity.setAttribute(Attribute.VELOCITY,
                newPolygonPos.sub(polygonOldPos).scale(1.0f / (float) delta));
        polygonEntity.setAttribute(Attribute.POSITION, newPolygonPos);

        return true;
    }

    /**
     * Sends the collision message to both entities.
     * 
     * @param objectA
     *            First object
     * @param objectB
     *            Second object
     * @param delta
     *            Delta time
     * @param check
     *            Check if they collide. Not wanted if the check is already
     *            done.
     */
    public static void sendCollisionMessage(Entity objectA, Entity objectB,
            double delta, boolean check) {
        /* Gather some information for the collision event. */
        final Vector2f theorPosA = (Vector2f) objectA
                .getAttribute(Attribute.POSITION);
        final Vector2f theorPosB = (Vector2f) objectB
                .getAttribute(Attribute.POSITION);

        final Vector2f oldPosA = theorPosA.sub(((Vector2f) objectA
                .getAttribute(Attribute.VELOCITY)).scale((float) delta));
        final Vector2f oldPosB = theorPosA.sub(((Vector2f) objectB
                .getAttribute(Attribute.VELOCITY)).scale((float) delta));

        /* Kind of a hack. */
        final Vector2f posA = (Vector2f) objectA
                .getAttribute(Attribute.POSITION);
        final Vector2f posB = (Vector2f) objectB
                .getAttribute(Attribute.POSITION);

        final AbstractGeometry boundsA = ((AbstractGeometry) objectA
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate(theorPosA);
        final AbstractGeometry boundsB = ((AbstractGeometry) objectB
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate(theorPosB);

        if (!check || boundsA.intersects(boundsB)) {
            objectA.sendMessage(MessageType.COLLIDED, new CollisionData(posA,
                    oldPosA, theorPosA, delta, objectB));
            objectB.sendMessage(MessageType.COLLIDED, new CollisionData(posB,
                    oldPosB, theorPosB, delta, objectA));
        }
    }

    /**
     * Performs collision detection between entities other than a map.
     * 
     * @param entities
     *            Entities to check
     * @param staticMap
     * @param delta
     *            Delta time, used for interpolation
     */
    public static void calculateEntityCollisions(
            final Collection<Entity> entities, final QuadTree staticMap,
            final double delta) {
        final Entity[] entArray = entities.toArray(new Entity[0]);

        for (int i = 0; i < entArray.length - 1; i++) {
            for (int j = i + 1; j < entArray.length; j++) {
                if (!entArray[i].hasAttribute(Attribute.BOUNDING_GEOMETRY)
                        || !entArray[j]
                                .hasAttribute(Attribute.BOUNDING_GEOMETRY)) {
                    continue;
                }

                if (entArray[i].getFamily() == Family.FOAM_PARTICLE
                        || entArray[j].getFamily() == Family.FOAM_PARTICLE) {
                    continue;
                }

                if (entArray[i].hasAttribute(Attribute.NO_COLLIDE)
                        || entArray[j].hasAttribute(Attribute.NO_COLLIDE)) {
                    continue;
                }

                if (entArray[j].getAttribute(Attribute.VELOCITY).equals(
                        new Vector2f(0, 0))
                        && entArray[i].getAttribute(Attribute.VELOCITY).equals(
                                new Vector2f(0, 0))) {
                    continue;
                }

                sendCollisionMessage(entArray[i], entArray[j], delta, true);
            }
        }

        /* Only check the player and the foam for now. */
        for (final Entity element : entArray) {
            if (element.getFamily() == Family.PLAYER) {

                /* TODO: check the old position too! */
                final Rectangle rect = ((AbstractGeometry) element
                        .getAttribute(Attribute.BOUNDING_GEOMETRY))
                        .asRectangle().translate(
                                (Vector2f) element
                                        .getAttribute(Attribute.POSITION));

                final Vector2f theorPos = (Vector2f) element
                        .getAttribute(Attribute.POSITION);
                final Vector2f oldPos = theorPos.sub(((Vector2f) element
                        .getAttribute(Attribute.VELOCITY))
                        .scale(1 / (float) delta));

                final List<Entity> targetList = staticMap
                        .getObjectsFromRectangle(rect);

                if (targetList != null) {
                    for (final Entity target : targetList) {
                        if (resolvePolygonCircleCollision(element, target,
                                delta)) {
                            element.sendMessage(
                                    MessageType.COLLIDED,
                                    new CollisionData((Vector2f) element
                                            .getAttribute(Attribute.POSITION),
                                            oldPos, theorPos, delta, target));
                        }
                    }
                }

                break;
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
        final float tileSize = (Float) map.getAttribute(Attribute.TILE_WIDTH);

        for (final Entity ent : entities) {
            if (ent.hasAttribute(Attribute.BOUNDING_GEOMETRY)
                    && !ent.equals(map)) {

                if (ent.hasAttribute(Attribute.NO_COLLIDE)) {
                    continue;
                }

                Vector2f vel = (Vector2f) ent.getAttribute(Attribute.VELOCITY);

                // skip static entities
                if (vel.equals(new Vector2f(0, 0))) {
                    continue;
                }

                vel = vel.scale((float) delta); // velocity per frame
                final AbstractGeometry bounds = (AbstractGeometry) ent
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

                /*
                 * If the object is touching the floor, lower its horizontal
                 * speed. If there is no collision, the damping factor is 1.
                 */
                float damping = 1.0f;

                // bottom check
                if (vel.getY() > 0
                        && (lb.getType().isSolid() || rb.getType().isSolid())) {
                    final int rest = (int) (rect.getBottom() / tileSize);
                    y = rest * tileSize - rect.getHeight() - eps;

                    damping = FLOOR_DAMPING;
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
                ent.setAttribute(
                        Attribute.VELOCITY,
                        new Vector2f((x - oldPos.getX()) * damping, y
                                - oldPos.getY()).scale((float) (1 / delta)));

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
