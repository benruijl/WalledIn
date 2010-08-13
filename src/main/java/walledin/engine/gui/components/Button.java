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
package walledin.engine.gui.components;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.Screen;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Vector2f;

public class Button extends Screen implements ScreenMouseEventListener {
    /** Button text. */
    private String text;
    /** Keeps track if move is hovering over button. */
    private boolean selected;

    public Button(final Screen parent, final String text, final Vector2f pos) {
        super(parent, parent.getManager().getFont("arial20")
                .getBoundingRect(text), 2);
        this.text = text;
        setPosition(pos);
        addMouseEventListener(this);
        show();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update(final double delta) {
        selected = false;

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {

        final Font font = getManager().getFont("arial20");

        if (selected) {
            renderer.setColorRGB(1, 0, 0);
        }

        font.renderText(renderer, text, new Vector2f());
        renderer.setColorRGB(1, 1, 1);
        super.draw(renderer);
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
        selected = true;
    }
}
