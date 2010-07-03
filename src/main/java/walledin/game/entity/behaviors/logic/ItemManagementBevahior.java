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

    public ItemManagementBevahior(Entity owner, Set<ItemInfo> items) {
        super(owner);
        this.items = items;
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(double delta) {
        for (ItemInfo item : items) {
            item.update(delta, getOwner().getEntityManager());
        }

    }

}
