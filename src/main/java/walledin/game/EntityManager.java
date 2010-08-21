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
package walledin.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.network.server.ChangeSet;

public class EntityManager {
    private static final Logger LOG = Logger.getLogger(EntityManager.class);
    private final Map<String, Entity> entities;
    private final DrawOrderManager drawOrderManager;
    private final EntityFactory factory;
    private int uniqueNameCount = 0;
    private final Set<Entity> removed;
    private final Set<Entity> created;
    private int currentVersion;

    public EntityManager(final EntityFactory factory) {
        entities = new ConcurrentHashMap<String, Entity>();
        removed = new HashSet<Entity>();
        created = new HashSet<Entity>();
        this.factory = factory;
        drawOrderManager = new DrawOrderManager();
        currentVersion = 0;
    }

    /**
     * Creates a new Entity and adds it to the entities list.
     * 
     * @param familyName
     *            Family name
     * @param entityName
     *            Name of the entity
     * @return The created entity or null on failure
     */
    public Entity create(final Family familyName, final String entityName) {
        final Entity entity = factory.create(this, familyName, entityName);
        add(entity);
        return entity;
    }

    /**
     * Creates a new entity with a unique name and adds it to the entity list.
     * 
     * @param family
     *            Family of the entity
     * @return Entity or null on failure
     */
    public Entity create(Family family) {
        final Entity entity = factory.create(this, family,
                generateUniqueName(family));
        add(entity);
        return entity;
    }

    /**
     * Generates a unique name for an object. Useful when generating entities in
     * runtime. The entities will be named in the following format:
     * ENT_<i>familyname</i>_<i>num</i>, where <i>familyname</i> is the family
     * name and <i>num</i> is the number of already generated unique names.
     * 
     * @param family
     *            Name of the item's family
     * @return Unique entity name
     */
    public final String generateUniqueName(final Family family) {
        uniqueNameCount++;
        return "ENT_" + family.toString() + "_"
                + Integer.toString(uniqueNameCount);
    }

    /**
     * Adds an entity to the list.
     * 
     * @param entity
     *            Entity to add
     */
    public void add(final Entity entity) {
        /*
         * If entity already exists, check if the removed flag is set. If so,
         * unset that.
         */
        if (entities.containsKey(entity.getName())) {
            if (entity.isMarkedRemoved()) {
                entity.resetMarkedRemoved();
            } else {
                LOG.warn("Trying to add entity " + entity.getName()
                        + " , but entity already exists.");
            }

            return;
        }

        /*
         * If the entity is in the removed list, remove it from there. If not,
         * add it to the created list.
         */
        if (removed.contains(entity)) {
            removed.remove(entity);
        } else {
            created.add(entity);
        }

        entities.put(entity.getName(), entity);

        if (entity.hasAttribute(Attribute.Z_INDEX)) {
            drawOrderManager.add(entity);
        }
    }

    public void add(final Collection<Entity> entitiesList) {
        for (final Entity en : entitiesList) {
            add(en);
        }
    }

    /**
     * Removes the entity from the list, resets the markedRemoved flag and
     * resets the entity changed attributes list. This is required, because the
     * entity still exists and can be added later.
     * 
     * @param name
     *            Entity name
     * @return Removed entity
     */
    public Entity remove(final String name) {
        final Entity entity = entities.get(name);

        if (entity == null) {
            LOG.warn("Entity with name " + name + " is already removed.");
            return null;
        }

        if (created.contains(entity)) {
            // If the entity is already created in this update, remove it
            // because
            // it also removed in this update
            created.remove(entity);
            // Dont add it to removed because it never existed so we cannot
            // remove it
        } else {
            removed.add(entity);
        }

        drawOrderManager.removeEntity(entity);
        entities.remove(name);

        entity.resetMarkedRemoved();
        entity.resetAttributes();
        return entity;
    }

    public Entity get(final String name) {
        if (name == null) {
            return null;
        }
        return entities.get(name);

    }

    /**
     * Returns the entity list.
     * 
     * @return Entity list
     */
    public Map<String, Entity> getEntities() {
        return entities;
    }

    /**
     * Draws the entities using the draw order manager.
     * 
     * @param renderer
     *            Renderer
     */
    public void draw(final Renderer renderer) {
        drawOrderManager.draw(renderer);
    }

    public void update(final double delta) {
        /* Clean up entities which are flagged for removal */
        final List<Entity> removeList = new ArrayList<Entity>();
        for (final Entity entity : entities.values()) {
            if (entity.isMarkedRemoved()) {
                removeList.add(entity);
            }
        }

        for (int i = 0; i < removeList.size(); i++) {
            remove(removeList.get(i).getName());
        }

        for (final Entity entity : entities.values()) {
            entity.sendUpdate(delta);
        }
    }

    public void doCollisionDetection(final Entity curMap, final double delta) {
        CollisionManager.calculateMapCollisions(curMap, entities.values(),
                delta);
        CollisionManager.calculateEntityCollisions(entities.values(), delta);
    }

    public void init() {
    }

    public ChangeSet getChangeSet() {
        final ChangeSet result = new ChangeSet(currentVersion, created,
                removed, entities);
        created.clear();
        removed.clear();
        currentVersion++;
        return result;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}
