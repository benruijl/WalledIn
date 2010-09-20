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

import walledin.game.entity.Entity;

public interface EntityUpdateListener {
    /**
     * Called when a new game entity is created.
     * 
     * @param entity
     *            Game entity
     */
    void onEntityCreated(Entity entity);

    /**
     * Called when an entity gets removed from the game.
     * 
     * @param entity
     *            Game entity
     */
    void onEntityRemoved(Entity entity);

    /**
     * Called when the entity is updated by a changeset.
     * 
     * @param entity
     */
    void onEntityUpdated(Entity entity);
}
