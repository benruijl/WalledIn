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
package walledin.game.network.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

/**
 * Change set of the game state. A change set contains all the changes between
 * its version and the version of the current game state. The server is
 * responsible for keeping change sets up to date. Entrys in created should never be in removed.
 * Entrys in removed should not be in created or updated.
 * 
 * @author Wouter Smeenk
 * 
 */
public class ChangeSet {
	private final int version;
	private final Map<String, String> created;
	private final Set<String> removed;
	private final Map<String, Set<Attribute>> updated;

	public ChangeSet(int version, Set<Entity> created, Set<Entity> removed,
			Map<String, Entity> entities) {
		this.version = version;
		this.created = new HashMap<String, String>();
		this.removed = new HashSet<String>();
		this.updated = new HashMap<String, Set<Attribute>>();
		initialize(created, removed, entities);
	}

	/**
	 * creates the initial change set
	 * 
	 * @param created
	 * @param removed
	 * @param entities
	 */
	private void initialize(Set<Entity> created, Set<Entity> removed,
			Map<String, Entity> entities) {
		for (Entity entity : created) {
			this.created.put(entity.getName(), entity.getFamilyName());
		}
		for (Entity entity : removed) {
			this.removed.add(entity.getName());
		}
		for (Entity entity : entities.values()) {
			Set<Attribute> changes = entity.getChangedAttributes();
			if (!changes.isEmpty()) {
				updated.put(entity.getName(), changes);
			}
		}
	}

	/**
	 * Merges the change set into this one. It is assumed that the change set is
	 * more up to date then this one.
	 * 
	 * @param changeSet
	 */
	public void merge(ChangeSet changeSet) {
		// Add created to our created
		created.putAll(changeSet.created);
		// Add removed to our remved
		removed.addAll(changeSet.removed);
		
		// Add updates to our updates
		for (Entry<String, Set<Attribute>> entry: changeSet.updated.entrySet()) {
			String name = entry.getKey();
			Set<Attribute> ourChanges = updated.get(name);
			if (ourChanges == null) {
				// Create new changes if we done have it yet
				ourChanges = new HashSet<Attribute>();
			}
			// Add changes to our changes
			ourChanges.addAll(entry.getValue());
			updated.put(name, ourChanges);
		}
		
		// Remove removed entities from our created entities and updated entities
		for (String name: changeSet.removed) {
			created.remove(name);
			updated.remove(name);
		}
		
		// Remove created entities from our removed entities
		for (String name: changeSet.created.keySet()) {
			removed.remove(name);
		}
	}

	public int getVersion() {
		return version;
	}

	public Map<String, String> getCreated() {
		return created;
	}

	public Set<String> getRemoved() {
		return removed;
	}

	public Map<String, Set<Attribute>> getUpdated() {
		return updated;
	}
}
