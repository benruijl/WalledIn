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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author ben
 */
public class Input implements KeyListener {
	private static Input ref = null;
	private final Set<Integer> keysDown;

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private Input() {
		keysDown = new HashSet<Integer>();
	}

	public static Input getInstance() {
		if (ref == null) {
			ref = new Input();
		}

		return ref;
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // quick way out
			// FIXME ... very ugly .. no way to do some kind of cleanup ..
			// exit should be avoided in general
			System.exit(0);
		}

		keysDown.add(e.getKeyCode());
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		keysDown.remove(e.getKeyCode());
	}

	/**
	 * Set the key to the up state
	 * 
	 * @param key
	 */
	public void setKeyUp(int key) {
		keysDown.remove(key);
	}

	/**
	 * check if the key is down
	 * 
	 * @param key
	 *            keycode of the key
	 * @return if the key is down
	 */
	public boolean isKeyDown(final int key) {
		return keysDown.contains(key);
	}

	public Set<Integer> getKeysDown() {
		return new HashSet<Integer>(keysDown);
	}

}
