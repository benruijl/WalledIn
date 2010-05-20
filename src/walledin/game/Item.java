package walledin.game;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.ItemRenderBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;

public class Item extends Entity implements Cloneable {

	public Item(final String familyName, final String name, final String texPart,
			final Rectangle destRect, final Vector2f position,
			final Vector2f velocity) {
		super(familyName, name);

		addBehavior(new SpatialBehavior(this, position, velocity));
		addBehavior(new ItemRenderBehavior(this, texPart, destRect));

		setAttribute(Attribute.BOUNDING_RECT, destRect);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
