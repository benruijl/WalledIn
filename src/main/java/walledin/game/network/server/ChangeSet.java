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
 * responsible for keeping change sets up to date.
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
		created.putAll(changeSet.created);
		removed.addAll(changeSet.removed);
		
		for (Entry<String, Set<Attribute>> entry: changeSet.updated.entrySet()) {
			String name = entry.getKey();
			Set<Attribute> ourChanges = updated.get(name);
			if (ourChanges == null) {
				ourChanges = new HashSet<Attribute>();
			}
			ourChanges.addAll(entry.getValue());
			updated.put(name, ourChanges);
		}
		
		for (String name: changeSet.removed) {
			
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
