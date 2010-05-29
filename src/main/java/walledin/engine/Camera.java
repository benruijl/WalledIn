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
package walledin.engine;

import walledin.engine.math.Vector2f;

/**
 * 
 * @author ben
 */
public class Camera {
	private Vector2f pos;
	private Vector2f scale;
	private float rot;

	public Camera() {
		pos = new Vector2f();
		scale = new Vector2f(1.0f, 1.0f);
	}

	public Vector2f getPos() {
		return pos;
	}

	public void setPos(final Vector2f pos) {
		this.pos = pos;
	}

	/**
	 * Get rotation in <b>radians</b>
	 * 
	 * @return Returns rotation in radians
	 */
	public float getRot() {
		return rot;
	}

	public void setRot(final float rot) {
		this.rot = rot;
	}

	public Vector2f getScale() {
		return scale;
	}

	public void setScale(final Vector2f scale) {
		this.scale = scale;
	}

}
