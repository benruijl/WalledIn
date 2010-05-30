package walledin.game.entity.behaviors;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class WeaponBehavior extends Behavior {
	private final int fireLag;
	private boolean canShoot;
	private int lastShot; // frame of last shot

	public WeaponBehavior(Entity owner, int fireLag) {
		super(owner);
		this.fireLag = fireLag;
		this.lastShot = fireLag;
		this.canShoot = true;
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.SHOOT)
			if (canShoot) {
				Entity player = (Entity) data;

				final int or = (Integer) player
						.getAttribute(Attribute.ORIENTATION);
				final Vector2f playerPos = (Vector2f) player
						.getAttribute(Attribute.POSITION);
				
				final Vector2f bulletPosition;

				// slightly more complicated, since the player pos is defined as
				// the top left
				if (or > 0)
					bulletPosition = playerPos.add(new Vector2f(50.0f, 20.0f));
				else
					bulletPosition = playerPos.add(new Vector2f(0.0f, 20.0f));

				final Vector2f bulletVelocity = new Vector2f(or * 400.0f, 0);

				final EntityManager manager = getEntityManager();
				final Entity bullet = manager.create("bullet", manager
						.generateUniqueName("bullet"));

				bullet.setAttribute(Attribute.POSITION, bulletPosition);
				bullet.setAttribute(Attribute.VELOCITY, bulletVelocity);

				canShoot = false;
				lastShot = fireLag;
			}
	}

	@Override
	public void onUpdate(double delta) {
		if (!canShoot) {
			lastShot--;

			if (lastShot <= 0)
				canShoot = true;
		}

	}

}
