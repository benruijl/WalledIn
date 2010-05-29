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
package walledin.engine.math;

public class Circle {
	private Vector2f pos;
	private float radius;

	public Circle(final Vector2f pos, final float radius) {
		super();
		this.pos = pos;
		this.radius = radius;
	}

	public Circle() {
		pos = new Vector2f();
	}

	public Vector2f getPos() {
		return pos;
	}

	public float getRadius() {
		return radius;
	}

	public void setPos(final Vector2f pos) {
		this.pos = pos;
	}

	public Circle addPos(final Vector2f pos) {
		return new Circle(this.pos.add(pos), radius);
	}

	public void setRadius(final float radius) {
		this.radius = radius;
	}

	public boolean pointInSphere(final Vector2f pos) {
		return pos.sub(this.pos).lengthSquared() < radius * radius;
	}

	public boolean intersects(final Circle circ) {
		return (getRadius() + circ.getRadius())
				* (getRadius() + circ.getRadius()) > getPos()
				.sub(circ.getPos()).lengthSquared();
	}

	public static Circle fromRect(final Rectangle rect) {
		final Vector2f center = rect.getLeftTop().add(rect.getRightBottom())
				.scale(0.5f).add(rect.getLeftTop());
		final float radius = (float) (0.5 * Math.sqrt(rect.getWidth()
				+ rect.getHeight()));
		return new Circle(center, radius);
	}

}
