package walledin.game.entity;

import walledin.engine.Vector2f;

public enum Attribute {
	POSITION(Vector2f.class),
	VELOCITY(Vector2f.class),
	ORIENTATION(Integer.class),
	WALKANIMFRAME(Float.class);

	public final Class<?> clazz;

	private Attribute(Class<?> clazz) {
		this.clazz = clazz;
	}
}
