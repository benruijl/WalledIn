package walledin.game.entity.behaviors.logic;

import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class FoamParticleBehavior extends Behavior {

    public FoamParticleBehavior(Entity owner) {
        super(owner);
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
    }

    @Override
    public void onUpdate(double delta) {
        int health = (Integer) getAttribute(Attribute.HEALTH);
        
        if (health == 0) {
            getOwner().remove();
        }
    }

}
