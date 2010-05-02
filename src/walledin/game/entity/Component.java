package walledin.game.entity;

public class Component {
	private Entity owner;
	
	public void setOwner(Entity owner) {
		if (owner != null) {
			throw new IllegalArgumentException("Cannot set owner before detaching");
		}
		this.owner = owner;
	}

	public void detachFromOwner() {
		this.owner = null;
	}

}
