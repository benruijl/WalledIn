package walledin.game.entity.behaviors.logic;

import java.util.Set;

import walledin.game.ItemInfo;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

/**
 * This class takes care of the regeneration of items. It should be owned by a
 * Map.
 * 
 * @author Ben Ruijl
 * 
 */
public class ItemManagementBevahior extends Behavior {
    /** Set of information about items */
    Set<ItemInfo> items;

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
