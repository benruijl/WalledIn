package walledin.game.entity.behaviors;

import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthBehavior extends Behavior {
	private int health;
	private final int maxHealth;

	public HealthBehavior(final Entity owner, final int maxHealth,
			final int curHealth) {
		super(owner);

		health = curHealth;
		this.maxHealth = maxHealth;
	}

	@Override
	public void onMessage(final MessageType messageType, final Object data) {
		if (messageType == MessageType.RESTORE_HEALTH) {
			final int hp = (Integer) data;
			if (health + hp > maxHealth) {
				health = maxHealth;
			} else {
				health += hp;
			}
		}

	}

	@Override
	public void onUpdate(final double delta) {
		// TODO Auto-generated method stub

	}

}
