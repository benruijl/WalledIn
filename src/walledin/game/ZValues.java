package walledin.game;

public enum ZValues {
	MAP(-10),
	PLAYER(1),
	ITEM(0);
	
	public final int z;
	
	private ZValues(int z)
	{
		this.z = z;
	}
}
