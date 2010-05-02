package walledin.game.components;

import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.game.entity.Entity;

public abstract class Component implements RenderListener {
	private Entity owner;

	public void setOwner(Entity owner) {
		if (owner != null) {
			throw new IllegalArgumentException(
					"Cannot set owner before detaching");
		}
		this.owner = owner;
	}

	public void detachFromOwner() {
		this.owner = null;
	}

	@Override
	public void draw(final Renderer renderer) {
		
	}

	@Override
	public void update(final double delta) {
		
	}
	
	@Override
	public void init() {
		
	}
}
