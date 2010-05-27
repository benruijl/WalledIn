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
package walledin.game.map;

/**
 * This class keeps track of information of a single tile in a map. It stores
 * the tile type and the coordinates.
 * 
 * @author Ben Ruijl
 */
public class Tile {
	private final TileType type;
	private final int x;
	private final int y;

	public Tile(final TileType type, final int x, final int y) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	public TileType getType() {
		return type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
