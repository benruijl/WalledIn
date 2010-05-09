package walledin.game.entity;

import walledin.engine.Rectangle;
import walledin.engine.Vector2f;
import walledin.game.Tile;

public enum Attribute {
	POSITION(Vector2f.class),
	VELOCITY(Vector2f.class),
	ORIENTATION(Integer.class),
	WALK_ANIM_FRAME(Float.class),
	NAME(String.class),
	WIDTH(Integer.class),
	HEIGHT(Integer.class),
	TILES(Tile.class),
	BOUNDING_BOX(Rectangle.class),
	Z_INDEX(Integer.class);

	public final Class<?> clazz;

	private Attribute(Class<?> clazz) {
		this.clazz = clazz;
	}
}
