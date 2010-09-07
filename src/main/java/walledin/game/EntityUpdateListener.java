package walledin.game;

import walledin.game.entity.Entity;

public interface EntityUpdateListener {
    void onEntityCreated(Entity entity);

    void onEntityRemoved(Entity entity);
}
