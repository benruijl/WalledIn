/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
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
        final Rectangle rect2 = new Rectangle(9, 5, 1, 8);
        final Circle circ1 = rect1.asCircumscribedCircle();
        final Circle circ2 = rect2.asCircumscribedCircle();

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
