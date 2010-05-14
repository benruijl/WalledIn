package walledin.game;

import java.util.Collection;
import java.util.List;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMap;
import walledin.game.map.Tile;

public class CollisionManager {
	public CollisionManager() {
		// TODO Auto-generated constructor stub
	}

	private Tile tileFromPixel(GameMap map, Vector2f pos) {
		float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);
		int width = map.getAttribute(Attribute.WIDTH);

		List<Tile> tiles = map.getAttribute(Attribute.TILES);

		return tiles.get((int) (pos.x / tileSize) + width
				* (int) (pos.y / tileSize));
	}

	public void calculateCollisions(GameMap map, Collection<Entity> entities,
			double delta) {
		float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);

		for (Entity ent : entities)
			if (ent.hasAttribute(Attribute.BOUNDING_BOX)
					&& !ent.hasAttribute(Attribute.TILES)) {
				Rectangle rect2 = ent.getAttribute(Attribute.BOUNDING_BOX);
				Vector2f vel2 = ent.getAttribute(Attribute.VELOCITY);
				vel2 = vel2.scale((float) delta); // vel per frame

				if (vel2.x == 0 && vel2.y == 0) // deterministic float
												// comparison?
					continue;

				Vector2f endpos2 = ent.getAttribute(Attribute.POSITION);
				Vector2f oldpos2 = endpos2.sub(vel2);

				float x = endpos2.x;
				float y = endpos2.y;
				float eps = 0.001f;

				// VERTICAL CHECK - move vertically only
				rect2 = rect2
						.setPos(new Vector2f(oldpos2.x, oldpos2.y + vel2.y));

				// check the four edges
				Tile lt = tileFromPixel(map, rect2.getLeftTop());
				Tile lb = tileFromPixel(map, rect2.getLeftBottom());
				Tile rt = tileFromPixel(map, rect2.getRightTop());
				Tile rb = tileFromPixel(map, rect2.getRightBottom());

				// bottom check
				if (vel2.y > 0
						&& (lb.getType().isSolid() || rb.getType().isSolid())) {
					int rest = (int) (rect2.getBottom() / tileSize);
					y = rest * tileSize - rect2.getHeight() - eps;
				} else
				// top check
				if (vel2.y < 0
						&& (lt.getType().isSolid() || rt.getType().isSolid())) {
					int rest = (int) (rect2.getTop() / tileSize);
					y = (rest + 1) * tileSize + eps;
				}

				// HORIZONTAL CHECK - move horizontally only
				rect2 = rect2.setPos(new Vector2f(oldpos2.x + vel2.x, y)); // new
																			// position
				lt = tileFromPixel(map, rect2.getLeftTop());
				lb = tileFromPixel(map, rect2.getLeftBottom());
				rt = tileFromPixel(map, rect2.getRightTop());
				rb = tileFromPixel(map, rect2.getRightBottom());

				// right check
				if (vel2.x > 0
						&& (rt.getType().isSolid() || rb.getType().isSolid())) {
					int rest = (int) (rect2.getRight() / tileSize);
					x = rest * tileSize - rect2.getWidth() - eps;
				} else
				// left check
				if (vel2.x < 0
						&& (lt.getType().isSolid() || lb.getType().isSolid())) {
					int rest = (int) (rect2.getLeft() / tileSize);
					x = (rest + 1) * tileSize + eps;
				}

				ent.setAttribute(Attribute.POSITION, new Vector2f(x, y));
				ent.setAttribute(Attribute.VELOCITY, new Vector2f(0, 0));
			}

	}

}
