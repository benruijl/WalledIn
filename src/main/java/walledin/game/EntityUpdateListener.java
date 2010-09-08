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
