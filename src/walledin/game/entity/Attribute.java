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
package walledin.game.entity;

import java.util.List;

import walledin.engine.math.Circle;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public enum Attribute {
	POSITION(Vector2f.class),
	VELOCITY(Vector2f.class),
	ORIENTATION(Integer.class),
	WALK_ANIM_FRAME(Float.class),
	NAME(String.class),
	WIDTH(Integer.class),
	HEIGHT(Integer.class),
	TILES(List.class),
	BOUNDING_RECT(Rectangle.class),
	BOUNDING_CIRCLE(Circle.class),
	Z_INDEX(Integer.class),
	ITEM_LIST(List.class),
	RENDER_TILE_SIZE(Float.class), 
	HEALTH(Integer.class);

	public final Class<?> clazz;

	private Attribute(final Class<?> clazz) {
		this.clazz = clazz;
	}
}
