package walledin.game.entity;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private final Map<Class<? extends Component>, Component> components;

	public Entity() {
		components = new HashMap<Class<? extends Component>, Component>();
	}

	public void addComponent(Component component) {
		Class<? extends Component> clazz = component.getClass();
		if (components.containsKey(clazz)) {
			throw new IllegalArgumentException("Entity [" + toString()
					+ "] already contains Component of class: "
					+ component.getClass().getName());
		}
		component.setOwner(this);
		components.put(clazz, component);
	}
	
	public Component removeComponent(Class<? extends Component> clazz) {
		Component component = components.remove(clazz);
		component.detachFromOwner();
		return component;
	}
}
