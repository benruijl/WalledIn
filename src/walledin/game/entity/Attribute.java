package walledin.game.entity;

import java.util.List;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public enum Attribute {
	POSITION(Vector2f.class, true), VELOCITY(Vector2f.class, true), WIDTH(
			Integer.class, true), HEIGHT(Integer.class, true), TILES(
			List.class, true), ITEM_LIST(List.class, true),

	ORIENTATION(Integer.class), WALK_ANIM_FRAME(Float.class), BOUNDING_RECT(
			Rectangle.class), BOUNDING_CIRCLE(Circle.class), Z_INDEX(
			Integer.class), RENDER_TILE_SIZE(Float.class);

	public final Class<?> clazz;
	public final boolean sendOverNetwork;

	private Attribute(final Class<?> clazz) {
		this(clazz, false);
	}

	private Attribute(final Class<?> clazz, final boolean sendOverNetwork) {
		this.clazz = clazz;
		this.sendOverNetwork = sendOverNetwork;
	}
}
