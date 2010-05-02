/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic resource manager.
 * 
 * @author ben
 */
public class ResourceManager<U, T> {
	private final Map<U, T> mResList;

	public ResourceManager() {
		mResList = new HashMap<U, T>();
	}

	boolean insert(final U key, final T value) {
		if (mResList.containsKey(key)) {
			System.err.print("Key " + key.toString() + "already exists");
			return false;
		}

		mResList.put(key, value);
		return true;
	}

	Integer count() {
		return mResList.size();
	}

	T get(final U key) {
		return mResList.get(key);
	}
}
