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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class DrawOrderManager {
    private static final Logger LOG = Logger.getLogger(DrawOrderManager.class);

    private static class ZOrderComperator implements Comparator<Entity> {
        @Override
        public int compare(final Entity o1, final Entity o2) {
            final int zA = (Integer) o1.getAttribute(Attribute.Z_INDEX);
            final int zB = (Integer) o2.getAttribute(Attribute.Z_INDEX);

            if (zA == zB) {
                return o1.getName().compareTo(o2.getName());
            }

            return zA - zB;
        }
    }

    private final SortedSet<Entity> entities;
    private final Set<Entity> addLater;
    private final Set<Entity> removeLater;

    public DrawOrderManager() {
        super();
        entities = new TreeSet<Entity>(new ZOrderComperator());
        addLater = new HashSet<Entity>();
        removeLater = new HashSet<Entity>();
    }

    /**
     * Add a list of entities to a list sorted on z-index
     * 
     * @param Collection
     *            of entities to be added
     */
    public void add(final Collection<Entity> entitiesList) {
        for (final Entity en : entitiesList) {
            add(en);
        }
    }

    /**
     * Add entity to a list sorted on z-index
     * 
     * @param e
     *            Entity to be added
     * @return True if added, false if not
     */
    public boolean add(final Entity e) {
        if (!e.hasAttribute(Attribute.Z_INDEX)) {
            return false;
        }
        return addLater.add(e);
    }

    public void removeEntity(final Entity entity) {
        if (entity == null) {
            LOG.debug("removing null!");
        }
        removeLater.add(entity);
    }

    public SortedSet<Entity> getList() {
        return entities;
    }

    public void draw(final Renderer renderer) {
        entities.addAll(addLater);
        entities.removeAll(removeLater);
        addLater.clear();
        removeLater.clear();
        /* Draw all entities in the correct order */
        for (final Entity ent : entities) {
            ent.sendMessage(MessageType.RENDER, renderer);
        }
    }
}
