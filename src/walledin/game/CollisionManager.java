package walledin.game;

import java.util.Collection;
import java.util.List;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.map.Tile;
import walledin.math.Circle;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class CollisionManager {

	static public class CollisionData {
		private final Vector2f newPos;
		private final Vector2f oldPos;
		private final Vector2f theorPos;
		private final Entity collisionEntity;

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

	}

	static private Tile tileFromPixel(final Entity map, final Vector2f pos) {
		final float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);
		final int width = map.getAttribute(Attribute.WIDTH);

		final List<Tile> tiles = map.getAttribute(Attribute.TILES);

		return tiles.get((int) (pos.x / tileSize) + width
				* (int) (pos.y / tileSize));
	}

	static public void calculateEntityCollisions(
			final Collection<Entity> entities, final double delta) {
		Entity[] entArray = new Entity[0];
		entArray = entities.toArray(entArray);

		for (int i = 0; i < entArray.length - 1; i++) {
			for (int j = i + 1; j < entArray.length; j++) {
				if (!entArray[i].hasAttribute(Attribute.BOUNDING_RECT)
						|| !entArray[j].hasAttribute(Attribute.BOUNDING_RECT)) {
					continue;
				}

				Circle circA = entArray[i]
						.getAttribute(Attribute.BOUNDING_CIRCLE);
				Circle circB = entArray[j]
						.getAttribute(Attribute.BOUNDING_CIRCLE);

				circA = circA.addPos((Vector2f) entArray[i]
						.getAttribute(Attribute.POSITION));
				circB = circB.addPos((Vector2f) entArray[j]
						.getAttribute(Attribute.POSITION));

				if (!circA.intersects(circB)) {
					continue;
				}

				Rectangle rectA = entArray[i]
						.getAttribute(Attribute.BOUNDING_RECT);
				Rectangle rectB = entArray[j]
						.getAttribute(Attribute.BOUNDING_RECT);

				rectA = rectA.translate((Vector2f) entArray[i]
						.getAttribute(Attribute.POSITION));
				rectB = rectB.translate((Vector2f) entArray[j]
						.getAttribute(Attribute.POSITION));

				if (!rectA.intersects(rectB)) {
					continue;
				}

				final Vector2f posA = entArray[i]
						.getAttribute(Attribute.POSITION);
				final Vector2f posB = entArray[j]
						.getAttribute(Attribute.POSITION);

				// no response yet, so give the same data
				entArray[i].sendMessage(MessageType.COLLIDED,
						new CollisionData(posA, posA, posA, entArray[j]));
				entArray[j].sendMessage(MessageType.COLLIDED,
						new CollisionData(posB, posB, posB, entArray[i]));
			}
		}
	}

	static public void calculateMapCollisions(final Entity map,
			final Collection<Entity> entities, final double delta) {
		final float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);

		for (final Entity ent : entities) {
			if (ent.hasAttribute(Attribute.BOUNDING_RECT) && !ent.equals(map)) {

				Vector2f vel = ent.getAttribute(Attribute.VELOCITY);

				if (vel.x == 0 && vel.y == 0) {
					continue;
				}

				vel = vel.scale((float) delta); // velocity per frame
				Rectangle rect = ent.getAttribute(Attribute.BOUNDING_RECT);
				final Vector2f curPos = ent.getAttribute(Attribute.POSITION);
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
				if (Math.abs(x - curPos.x) > 0.0001f || Math.abs(y - curPos.y) > 0.0001f)
				ent.sendMessage(MessageType.COLLIDED, new CollisionData(
						new Vector2f(x, y), oldPos, curPos, map));
			}
		}

	}

}
