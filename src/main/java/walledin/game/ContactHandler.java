package walledin.game;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;

import walledin.engine.physics.ContactListener;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;

public class ContactHandler implements ContactListener {
    private final EntityManager entityManager;

    public ContactHandler(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    @Override
    public void processContact(ManifoldPoint point,
            PersistentManifold contactManifold) {
        CollisionObject a = (CollisionObject) contactManifold.getBody0();
        CollisionObject b = (CollisionObject) contactManifold.getBody1();

        Entity ent1 = entityManager.get((String) a.getUserPointer());

        Entity ent2 = entityManager.get((String) b.getUserPointer());

        if (ent1 != null && ent2 != null) {
            ent1.sendMessage(MessageType.COLLIDED, ent2);
            ent2.sendMessage(MessageType.COLLIDED, ent1);
        }

    }
}
