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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import walledin.engine.math.Vector2i;

/**
 * 
 * @author ben
 */
public final class Input implements KeyListener, MouseListener, MouseMotionListener {
    private static Input ref = null;
    private final Set<Integer> keysDown;
    private Vector2i mousePos;

    private Input() {
        keysDown = new HashSet<Integer>();
        mousePos = new Vector2i();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
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
    public void setKeyUp(final int key) {
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

    @Override
    public void mouseDragged(final MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        mousePos = new Vector2i(e.getX(), e.getY());
    }

    public Vector2i getMousePos() {
        return mousePos;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mousePos = new Vector2i(e.getX(), e.getY());   
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

}
