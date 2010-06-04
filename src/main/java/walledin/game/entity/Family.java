package walledin.game.entity;

public enum Family {
	ROOT(null),
	WEAPON(ROOT),
	PLAYER(ROOT),
	ITEM(ROOT),
	HANDGUN(WEAPON),
	FOAMGUN(WEAPON),
	HEALTHKIT(ITEM),
	ARMOURKIT(ITEM),
	BULLET(ROOT),
	HANDGUN_BULLET(BULLET),
	FOAMGUN_BULLET(BULLET),
	MAP(ROOT),
	BACKGROUND(ROOT),
	FOAM_PARTICLE(BULLET);
	
	Family parent;
	
	private Family(Family parent) {
		this.parent = parent;
	}
	
	public Family getParent() {
		return parent;
	}
}
