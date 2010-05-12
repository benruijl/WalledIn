package walledin.game.entity.behaviors;

import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class HealthBehavior extends Behavior {
	private int health;
	private int maxHealth;

	public HealthBehavior(Entity owner, int maxHealth, int curHealth) {
		super(owner);

		this.health = curHealth;
		this.maxHealth = maxHealth;
	}

	@Override
	public void onMessage(MessageType messageType, Object data) {
		if (messageType == MessageType.RESTORE_HEALTH)
		{
			int hp = (Integer)data;
			if (health + hp > maxHealth)
				health = maxHealth;
			else
				health += hp;
		}

	}

	@Override
	public void onUpdate(double delta) {
		// TODO Auto-generated method stub

	}

}
