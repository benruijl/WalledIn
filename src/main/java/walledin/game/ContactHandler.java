package walledin.game;

import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;

import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class ContactHandler implements ContactListener {
    private final EntityManager entityManager;

    public ContactHandler(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    @Override
    public void add(ContactPoint point) {
        Entity ent1 = entityManager.get((String) point.shape1.getBody()
                .getUserData());

        Entity ent2 = entityManager.get((String) point.shape2.getBody()
                .getUserData());

        if (ent1 != null && ent2 != null) {
            ent1.sendMessage(MessageType.COLLIDED, ent2);
            ent2.sendMessage(MessageType.COLLIDED, ent1);
        }
    }

    @Override
    public void persist(ContactPoint point) {
    }

    @Override
    public void remove(ContactPoint point) {
    }

    @Override
    public void result(ContactResult point) {
    }
}
