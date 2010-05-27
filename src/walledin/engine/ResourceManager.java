/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

*/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A generic resource manager.
 * 
 * @author ben
 */
public class ResourceManager<U, T> {
	private final static Logger LOG = Logger.getLogger(ResourceManager.class);
	private final Map<U, T> resources;

	public ResourceManager() {
		resources = new HashMap<U, T>();
	}

	protected boolean put(final U key, final T value) {
		if (resources.containsKey(key)) {
			LOG.warn("Key " + key.toString() + "already exists");
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
