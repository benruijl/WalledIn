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
				
				Vector2f vel = ent.getAttribute(Attribute.VELOCITY);
				
				if (vel.x == 0 && vel.y == 0)
					continue;

				vel = vel.scale((float) delta); // velocity per frame
				Rectangle rect = ent.getAttribute(Attribute.BOUNDING_BOX);
				Vector2f curPos = ent.getAttribute(Attribute.POSITION);
				Vector2f oldPos = curPos.sub(vel);

				float x = curPos.x; // new x position after collision
				float y = curPos.y; // new y position after collision
				float eps = 0.001f; // small value to prevent floating errors

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
					int rest = (int) (rect.getBottom() / tileSize);
					y = rest * tileSize - rect.getHeight() - eps;
				} else
				// top check
				if (vel.y < 0
						&& (lt.getType().isSolid() || rt.getType().isSolid())) {
					int rest = (int) (rect.getTop() / tileSize);
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
					int rest = (int) (rect.getRight() / tileSize);
					x = rest * tileSize - rect.getWidth() - eps;
				} else
				// left check
				if (vel.x < 0
						&& (lt.getType().isSolid() || lb.getType().isSolid())) {
					int rest = (int) (rect.getLeft() / tileSize);
					x = (rest + 1) * tileSize + eps;
				}

				ent.setAttribute(Attribute.POSITION, new Vector2f(x, y));
				ent.setAttribute(Attribute.VELOCITY, new Vector2f(x - oldPos.x, y - oldPos.y));
			}

	}

}
