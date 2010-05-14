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
	
	private Tile tileFromPixel(GameMap map, Vector2f pos)
	{
		float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);
		int width = map.getAttribute(Attribute.WIDTH);
		
		List<Tile> tiles = map.getAttribute(Attribute.TILES);

		return tiles.get((int) (pos.x / tileSize) + width * (int)(pos.y / tileSize));
	}
	
	public void calculateCollisions(GameMap map, Collection<Entity> entities, double delta) {
		float tileSize = map.getAttribute(Attribute.RENDER_TILE_SIZE);
		
		for (Entity ent : entities)
			if (ent.hasAttribute(Attribute.BOUNDING_BOX) && !ent.hasAttribute(Attribute.TILES))
			{
				Rectangle rect2 = ent.getAttribute(Attribute.BOUNDING_BOX);
				Vector2f vel2 = ent.getAttribute(Attribute.VELOCITY);
				vel2 = vel2.scale((float)delta); // vel per frame
				
				Vector2f endpos2 = ent.getAttribute(Attribute.POSITION);
				Vector2f oldpos2 = endpos2.sub(vel2);
				rect2 = rect2.translate(endpos2); // the theoretical final pos BB
				
				if (vel2.x == 0 && vel2.y == 0) // deterministic float comparison?
					continue;
				
				// check the four edges
				Tile lt = tileFromPixel(map, rect2.getLeftTop());
				Tile lb = tileFromPixel(map, rect2.getLeftBottom());
				Tile rt = tileFromPixel(map, rect2.getRightTop());
				Tile rb = tileFromPixel(map, rect2.getRightBottom());
				
				float x = endpos2.x;
				float y = endpos2.y;
				
				// vertical col. det
				if (vel2.y > 0 && (lb.getType().isSolid() || rb.getType().isSolid()))
				{
					int rest = (int)(rect2.getBottom() / tileSize);
					y = rest * tileSize  - rect2.getHeight();
				}
				
				ent.setAttribute(Attribute.POSITION, new Vector2f(x, y));
				//ent.setAttribute(Attribute.VELOCITY, new Vector2f(x - oldpos2.x, y - oldpos2.y));
				ent.setAttribute(Attribute.VELOCITY, new Vector2f(((Vector2f)ent.getAttribute(Attribute.VELOCITY)).x, 0));
			}
		
	}

}
