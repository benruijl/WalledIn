package walledin.engine.math.test;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import walledin.engine.math.Circle;
import walledin.engine.math.Vector2f;

public class CircleTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetSetDefault() {
		Circle circle = new Circle();
		Assert.assertEquals(circle.getRadius(), 0f);
		Assert.assertEquals(circle.getPos(), new Vector2f(0,0));
	}
	
	@Test
	public void testGetSetNonDefault() {
		float radius = 3.5f;
		Vector2f pos = new Vector2f(2,3);
		Circle circle = new Circle(pos, radius);
		Assert.assertEquals(circle.getRadius(), radius);
		Assert.assertEquals(circle.getPos(), new Vector2f(2,3));
	}
	
	@Test
	public void testPointInSphere() {
		Circle circle = new Circle(new Vector2f(2,3), 3f);
		Vector2f point = new Vector2f(1,2);
		Assert.assertTrue(circle.pointInSphere(point));
		// TODO test more corner cases
	}
}
