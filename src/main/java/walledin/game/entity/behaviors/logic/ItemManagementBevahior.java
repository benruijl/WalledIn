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
package walledin.game.entity.behaviors.logic;

import java.util.Set;

import walledin.game.ItemInfo;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

/**
 * This class takes care of the regeneration of items. It should be owned by a
 * Map.
 * 
 * @author Ben Ruijl
 * 
 */
public class ItemManagementBevahior extends AbstractBehavior {
    /** Set of information about items */
    private final Set<ItemInfo> items;

    public ItemManagementBevahior(final Entity owner, final Set<ItemInfo> items) {
        super(owner);
        this.items = items;
    }

    @Override
    public void onMessage(final MessageType messageType, final Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(final double delta) {
        for (final ItemInfo item : items) {
            item.update(delta, getOwner().getEntityManager());
        }

    }

}
