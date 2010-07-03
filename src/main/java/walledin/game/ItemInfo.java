package walledin.game;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;

public class ItemInfo {
    private static final Logger LOG = Logger.getLogger(ItemInfo.class);

    Family family;
    Vector2f position;
    float respawnTime;
    float waitTime;
    Entity associatedEntity;
    boolean removed;

    public ItemInfo(Family family, Vector2f position, float respawnTime) {
        super();
        this.family = family;
        this.position = position;
        this.respawnTime = respawnTime;

        removed = false;
        waitTime = respawnTime;
        associatedEntity = null;
    }

    public Family getFamily() {
        return family;
    }

    public Vector2f getPosition() {
        return position;
    }

    public float getRespawnTime() {
        return respawnTime;
    }

    public void update(double delta, EntityManager entityManager) {
        /* Check if removed */
        removed = associatedEntity == null
                || Boolean.FALSE.equals(associatedEntity.getAttribute(Attribute.NOT_PICKED_UP));

        if (removed) {
            waitTime += delta;

            if (waitTime >= respawnTime) {
                associatedEntity = entityManager.create(family,
                        entityManager.generateUniqueName(family));

                LOG.info("Respawned item of family " + family);

                associatedEntity.setAttribute(Attribute.POSITION, position);
                associatedEntity.setAttribute(Attribute.NOT_PICKED_UP, Boolean.TRUE);

                removed = false;
                waitTime = 0.0f;
            }
        }
    }

    public boolean isRemoved() {
        return removed;
    }
}