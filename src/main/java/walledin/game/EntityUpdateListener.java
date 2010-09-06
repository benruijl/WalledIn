package walledin.game;

import walledin.game.entity.Entity;

public interface EntityUpdateListener {
    void entityCreated(Entity entity);

    void entityRemoved(Entity entity);
}
