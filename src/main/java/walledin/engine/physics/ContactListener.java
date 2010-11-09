package walledin.engine.physics;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;

public interface ContactListener {
    void processContact(ManifoldPoint point, PersistentManifold contactManifold);
}
