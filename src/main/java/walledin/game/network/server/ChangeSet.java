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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;

/**
 * Change set of the game state. A change set contains all the changes between
 * its version and the version of the current game state. The server is
 * responsible for keeping change sets up to date. Entries in created should
 * never be in removed. Entries in removed should not be in created or updated.
 * 
 * @author Wouter Smeenk
 * 
 */
public class ChangeSet {
    /** The first version this change set records changes from. */
    private final int version;
    /**
     * For each version this change set can update from the name and family of
     * the entities that have been created
     */
    private final List<Map<String, Family>> created;
    /**
     * For each version this change set can update from the name of the entities
     * that have been removed
     */
    private final List<Set<String>> removed;
    /**
     * For each entity the values of the attributes that have changed
     */
    private final Map<String, Map<Attribute, Object>> updated;

    public ChangeSet(final int version, final Set<Entity> created,
            final Set<Entity> removed, final Map<String, Entity> entities) {
        this.version = version;
        this.created = new ArrayList<Map<String, Family>>();
        this.removed = new ArrayList<Set<String>>();
        updated = new HashMap<String, Map<Attribute, Object>>();
        initialize(created, removed, entities);
    }

    public ChangeSet(final int version,
            final List<Map<String, Family>> created,
            final List<Set<String>> removed,
            final Map<String, Map<Attribute, Object>> updated) {
        this.version = version;
        this.created = created;
        this.removed = removed;
        this.updated = updated;
    }

    /**
     * Creates the initial change set.
     * 
     * @param created
     * @param removed
     * @param entities
     */
    private void initialize(final Set<Entity> created,
            final Set<Entity> removed, final Map<String, Entity> entities) {
        Map<String, Family> tempCreated = new HashMap<String, Family>();
        for (final Entity entity : created) {
            tempCreated.put(entity.getName(), entity.getFamily());
        }
        Set<String> tempRemoved = new HashSet<String>();
        for (final Entity entity : removed) {
            tempRemoved.add(entity.getName());
        }
        this.created.add(tempCreated);
        this.removed.add(tempRemoved);
        for (final Entity entity : entities.values()) {
            final Set<Attribute> changes = entity.getChangedAttributes();
            if (!changes.isEmpty()) {
                updated.put(entity.getName(), entity.getAttributes(changes));
            }
        }
    }

    /**
     * Merges the change set into this one. It is assumed that the change set is
     * more up to date then this one and only contains one version
     * 
     * @param changeSet
     */
    public void merge(final ChangeSet changeSet) {
        if (changeSet.created.size() > 1 || changeSet.removed.size() > 1) {
            throw new IllegalStateException(
                    "Cannot merge a changeset with multiple versions");
        }

        if (version + removed.size() != changeSet.version) {
            throw new IllegalStateException("Cannot merge a changeset with a"
                    + " version other than our oldest version + 1");
        }

        Map<String, Family> theirCreated = changeSet.created.get(0);
        Set<String> theirRemoved = changeSet.removed.get(0);
        // Add created to our created
        created.add(theirCreated);
        // Add removed to our removed
        removed.add(theirRemoved);

        // Add updates to our updates
        for (final Entry<String, Map<Attribute, Object>> entry : changeSet.updated
                .entrySet()) {
            final String name = entry.getKey();
            Map<Attribute, Object> ourChanges = updated.get(name);
            if (ourChanges == null) {
                // Create new changes if we dont have it yet
                ourChanges = new HashMap<Attribute, Object>();
            }
            // Add changes to our changes
            ourChanges.putAll(entry.getValue());
            updated.put(name, ourChanges);
        }

        // Remove removed entities from updated
        // entities
        for (final String name : theirRemoved) {
            updated.remove(name);
        }
    }

    public Map<String, Family> getCreatedFromVersion(int firstVersion) {
        Map<String, Family> result = new HashMap<String, Family>();
        for (int i = version - firstVersion; i < created.size(); i++) {
            Map<String, Family> currentCreated = created.get(i);
            Set<String> currentRemoved = removed.get(i);
            result.putAll(currentCreated);
            for (String name : currentRemoved) {
                result.remove(name);
            }
        }
        return result;
    }

    public Set<String> getRemovedFromVersion(int firstVersion) {
        Set<String> result = new HashSet<String>();
        for (int i = version - firstVersion; i < removed.size(); i++) {
            Map<String, Family> currentCreated = created.get(i);
            Set<String> currentRemoved = removed.get(i);
            result.addAll(currentRemoved);
            for (String name : currentCreated.keySet()) {
                result.remove(name);
            }
        }
        return result;
    }

    /**
     * Returns the version this change set records changes from.
     * 
     * @return the version this change set records changes from.
     */
    public int getVersion() {
        return version;
    }

    public List<Map<String, Family>> getCreated() {
        return created;
    }

    public List<Set<String>> getRemoved() {
        return removed;
    }

    public Map<String, Map<Attribute, Object>> getUpdated() {
        return updated;
    }
}
