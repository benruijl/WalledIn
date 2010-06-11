package walledin.engine.math;

import walledin.util.Utils;

public abstract class Geometry {
    public abstract boolean intersects(Geometry geometry);

    public abstract boolean intersects(Rectangle rect);

    public abstract boolean intersects(Circle circ);

    public static boolean intersects(final Rectangle rect, final Circle circ) {
        final float closestX = Utils.clamp(circ.getPos().getX(),
                rect.getLeft(), rect.getRight());
        final float closestY = Utils.clamp(circ.getPos().getY(), rect.getTop(),
                rect.getBottom());

        final Vector2f dist = circ.getPos().sub(
                new Vector2f(closestX, closestY));

        return dist.lengthSquared() < circ.getRadius() * circ.getRadius();
    }

    public abstract Rectangle asRectangle();

    public abstract Circle asCircumscribedCircle();

    public abstract Circle asInscribedCircle();

    public abstract Geometry translate(final Vector2f pos);
}
