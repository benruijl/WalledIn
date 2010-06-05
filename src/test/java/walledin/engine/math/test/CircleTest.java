package walledin.engine.math.test;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class CircleTest {
    private static final Logger LOG = Logger.getLogger(CircleTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSetDefault() {
        final Circle circle = new Circle();
        Assert.assertEquals(circle.getRadius(), 0f);
        Assert.assertEquals(circle.getPos(), new Vector2f(0, 0));
    }

    @Test
    public void testGetSetNonDefault() {
        final float radius = 3.5f;
        final Vector2f pos = new Vector2f(2, 3);
        final Circle circle = new Circle(pos, radius);
        Assert.assertEquals(circle.getRadius(), radius);
        // assumes equals for vector
        Assert.assertEquals(circle.getPos(), new Vector2f(2, 3));
    }

    @Test
    public void testPointInSphere() {
        final Circle circle = new Circle(new Vector2f(2, 3), 3f);
        final Vector2f point = new Vector2f(1, 2);
        Assert.assertTrue(circle.pointInSphere(point));
        // TODO test more corner cases
    }

    @Test
    public void testPerformance() {
        final Rectangle rect1 = new Rectangle(1, 2, 3, 4);
        final Rectangle rect2 = new Rectangle(34322, 2343, 2030, 43223);
        final Circle circ1 = Circle.fromRect(rect1);
        final Circle circ2 = Circle.fromRect(rect2);

        doPreformanceTest(rect1, rect2, circ1, circ2, 100000);
        doPreformanceTest(rect1, rect2, circ1, circ2, 1000000);
        doPreformanceTest(rect1, rect2, circ1, circ2, 10000000);
        doPreformanceTest(rect1, rect2, circ1, circ2, 100000000);
        doPreformanceTest(rect1, rect2, circ1, circ2, 10);
        doPreformanceTest(rect1, rect2, circ1, circ2, 100);
        doPreformanceTest(rect1, rect2, circ1, circ2, 1000);
        doPreformanceTest(rect1, rect2, circ1, circ2, 10000);

    }

    private void doPreformanceTest(final Rectangle rect1,
            final Rectangle rect2, final Circle circ1, final Circle circ2,
            final int amount) {
        long time = System.nanoTime();
        for (int i = 0; i < amount; i++) {
            rect1.intersects(rect2);
        }
        double diff = System.nanoTime() - time;
        double each = diff / amount;

        LOG.info(amount + " Rectangle intersects " + each
                + " nanoseconds per intersect total: " + diff);

        time = System.nanoTime();
        for (int i = 0; i < amount; i++) {
            circ1.intersects(circ2);
        }
        diff = System.nanoTime() - time;
        each = diff / amount;

        LOG.info(amount + " Circle intersects " + each
                + " nanoseconds per intersect total: " + diff);
    }
}
