/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ben
 */
public class Input implements KeyListener {

	public enum KeyState {

		Up, Down
	}

	private static Input ref = null;
	private final Map<Integer, KeyState> mKeyStates;

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void keyTyped(final KeyEvent e) {
	}

	public void keyPressed(final KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // quick way out
			// FIXME ... very ugly .. no way to do some kind of cleanup ..
			// exit should be avoided in general
			System.exit(0);
		}

		mKeyStates.put(e.getKeyCode(), KeyState.Down);
	}

	public void keyReleased(final KeyEvent e) {
		mKeyStates.put(e.getKeyCode(), KeyState.Up);
	}

	public boolean keyDown(final int nKey) {
		if (!mKeyStates.containsKey(nKey)) {
			return false;
		}

		return mKeyStates.get(nKey) == KeyState.Down;
	}

	/**
	 * Flag the key nKey as being up.
	 * 
	 * @param nKey
	 *            The keycode of the key
	 */
	public void setKeyUp(final int nKey) {
		mKeyStates.put(nKey, KeyState.Up);
	}

	private Input() {
		mKeyStates = new HashMap<Integer, KeyState>();
	}

	public static Input getInstance() {
		if (ref == null) {
			ref = new Input();
		}

		return ref;
	}
}
