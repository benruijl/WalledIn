package walledin.game.entity;

import java.util.List;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public enum Attribute {
	POSITION(Vector2f.class),
	VELOCITY(Vector2f.class),
	ORIENTATION(Integer.class),
	WALK_ANIM_FRAME(Float.class),
	NAME(String.class),
	WIDTH(Integer.class),
	HEIGHT(Integer.class),
	TILES(List.class),
	BOUNDING_RECT(Rectangle.class),
	BOUNDING_CIRCLE(Circle.class),
	Z_INDEX(Integer.class),
	ITEM_LIST(List.class),
	RENDER_TILE_SIZE(Float.class);

	public final Class<?> clazz;

	private Attribute(final Class<?> clazz) {
		this.clazz = clazz;
	}
}
