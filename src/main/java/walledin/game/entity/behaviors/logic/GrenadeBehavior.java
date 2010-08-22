package walledin.game.entity.behaviors.logic;

import walledin.engine.math.Matrix2f;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Behavior;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;

public class GrenadeBehavior extends Behavior {
    /** Explode time in seconds. */
    private final double explodeTime = 3.0;
    /**
     * The number of particles created from the explosion. They will fly in
     * different directions.
     */
    private final int numberOfParticles = 4;
    private double time = 0;
    private Vector2f particleTarget = new Vector2f(0, 100.0f);
    private Vector2f particleAcc = new Vector2f(0, 30000.0f);

    public GrenadeBehavior(Entity owner) {
        super(owner);
    }

    @Override
    public void onMessage(MessageType messageType, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(double delta) {
        time += delta;

        if (time > explodeTime) {
            /* Explode! */

            for (int i = 0; i < numberOfParticles; i++) {
                Entity foamBullet = getOwner().getEntityManager().create(
                        Family.FOAMGUN_BULLET);
                foamBullet.setAttribute(Attribute.POSITION,
                        getAttribute(Attribute.POSITION));

                foamBullet.sendMessage(MessageType.APPLY_FORCE,
                new Matrix2f(i
                        * Math.PI / (double) numberOfParticles)
                        .apply(particleAcc));

                foamBullet.setAttribute(Attribute.TARGET, new Matrix2f(i
                        * Math.PI / (double) numberOfParticles)
                        .apply(particleTarget));

            }

            getOwner().remove();
        }

    }
}
