/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A generic resource manager.
 * 
 * @author ben
 */
public class ResourceManager<U, T> {
	private final static Logger LOG = Logger.getLogger(ResourceManager.class
			.getName());
	private final Map<U, T> resources;

	public ResourceManager() {
		resources = new HashMap<U, T>();
	}

	protected boolean put(final U key, final T value) {
		if (resources.containsKey(key)) {
			LOG.warning("Key " + key.toString() + "already exists");
			return false;
		}

		resources.put(key, value);
		return true;
	}

	protected int getCount() {
		return resources.size();
	}

	public T get(final U key) {
		return resources.get(key);
	}
}
