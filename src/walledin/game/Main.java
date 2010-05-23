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
package walledin.game;

import walledin.engine.Renderer;

public class Main {
	private final Game game;
	private final Renderer renderer;

	public Main() {
		renderer = new Renderer();
		game = new Game(renderer);
	}

	public void run() {
		renderer.initialize("WalledIn", 800, 600, false);
		renderer.addListener(game);
		renderer.beginLoop();
	}

	public static void main(final String[] args) {
		final Main main = new Main();
		main.run();
	}
}
