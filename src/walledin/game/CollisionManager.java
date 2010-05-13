package walledin.game;

import java.util.ArrayList;
import java.util.List;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class CollisionManager {
	private final List<Entity> entities;
	
	public CollisionManager(List<Entity> allEntities) {
		this.entities = new ArrayList<Entity>();
		for (Entity entity: allEntities) {
			// filter
		}
	}
	
	public void calculateCollisions() {
		
	}
	
	public void calculateResponse(Entity entity1, Entity entity2) {

	}
}
