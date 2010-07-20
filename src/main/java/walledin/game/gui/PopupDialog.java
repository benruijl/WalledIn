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
package walledin.game.gui;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.gui.components.Button;

public class PopupDialog extends Screen implements ScreenMouseEventListener {
    private final String text;
    private final Button okButton;

    public PopupDialog(final ScreenManager manager, final String text) {
        super(manager, new Rectangle(-10, -20, manager.getFont("arial20")
                .getTextWidth(text) + 20, 70), 4);
        setPosition(new Vector2f(300, 250));
        this.text = text;

        okButton = new Button(this, "OK", new Vector2f(getRectangle()
                .getWidth()
                / 2.0f
                - manager.getFont("arial20").getTextWidth("OK") / 2.0f,
                +getRectangle().getHeight() - 25));
        okButton.addMouseEventListener(this);
        addChild(okButton);

    }

    @Override
    public final void draw(final Renderer renderer) {
        renderer.setColorRGB(0.4f, 0.4f, 0.4f);
        renderer.drawFilledRect(getRectangle());
        renderer.setColorRGB(1.0f, 1.0f, 1.0f);

        getManager().getFont("arial20").renderText(renderer, text,
                new Vector2f(0, 0));

        super.draw(renderer);
    }

    @Override
    public final void initialize() {
    }

    @Override
    public final void onMouseDown(final ScreenMouseEvent e) {
        if (e.getScreen() == okButton) {
            dispose();
        }

    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
        // TODO Auto-generated method stub

    }

}
