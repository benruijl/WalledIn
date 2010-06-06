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
import java.util.Set;
import java.util.Map.Entry;

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
    private final int version;
    private final Map<String, Family> created;
    private final Set<String> removed;
    private final Map<String, Set<Attribute>> updated;

    public ChangeSet(final int version, final Set<Entity> created,
            final Set<Entity> removed, final Map<String, Entity> entities) {
        this.version = version;
        this.created = new HashMap<String, Family>();
        this.removed = new HashSet<String>();
        updated = new HashMap<String, Set<Attribute>>();
        initialize(created, removed, entities);
    }

    /**
     * creates the initial change set
     * 
     * @param created
     * @param removed
     * @param entities
     */
    private void initialize(final Set<Entity> created,
            final Set<Entity> removed, final Map<String, Entity> entities) {
        for (final Entity entity : created) {
            this.created.put(entity.getName(), entity.getFamily());
        }
        for (final Entity entity : removed) {
            this.removed.add(entity.getName());
        }
        for (final Entity entity : entities.values()) {
            final Set<Attribute> changes = entity.getChangedAttributes();
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
    public void merge(final ChangeSet changeSet) {
        // Add created to our created
        created.putAll(changeSet.created);
        // Add removed to our remved
        removed.addAll(changeSet.removed);

        // Add updates to our updates
        for (final Entry<String, Set<Attribute>> entry : changeSet.updated
                .entrySet()) {
            final String name = entry.getKey();
            Set<Attribute> ourChanges = updated.get(name);
            if (ourChanges == null) {
                // Create new changes if we done have it yet
                ourChanges = new HashSet<Attribute>();
            }
            // Add changes to our changes
            ourChanges.addAll(entry.getValue());
            updated.put(name, ourChanges);
        }

        // Remove removed entities from our created entities and updated
        // entities
        for (final String name : changeSet.removed) {
            final Family removedFamily = created.remove(name);
            if (removedFamily != null) {
                // If there was something to be removed from the created set
                // then also remove it from the removed set because it has been
                // created and then removed between this version and the current
                // version, so there is no change.
                removed.remove(name);
            }
            updated.remove(name);
        }

        // Remove created entities from our removed entities
        for (final String name : changeSet.created.keySet()) {
            final boolean removedSomething = removed.remove(name);
            if (removedSomething) {
                // If there was something to be removed from the removed set
                // then also remove it from the created set because it has been
                // removed and then created again between this version and the
                // current version, so there is no change.
                removed.remove(name);
            }
        }
    }

    public int getVersion() {
        return version;
    }

    public Map<String, Family> getCreated() {
        return created;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public Map<String, Set<Attribute>> getUpdated() {
        return updated;
    }
}
