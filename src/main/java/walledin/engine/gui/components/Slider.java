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

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class Slider extends AbstractScreen implements ScreenMouseEventListener {
    private final float width = 80.0f;
    private final float height = 30.0f;

    private final List<String> sliderValues;
    private int currentIndex;

    public Slider(final AbstractScreen parent, final Rectangle boudingRect,
            final int z, final Vector2f pos) {
        super(parent, boudingRect, z);

        sliderValues = new ArrayList<String>();
        currentIndex = -1;
        setPosition(pos);
        addMouseEventListener(this);
        show();
    }

    public void addValue(final String value) {
        sliderValues.add(value);

        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    @Override
    public void draw(final Renderer renderer) {

        renderer.drawFilledRect(new Rectangle(0, 0, 5, height));
        renderer.drawFilledRect(new Rectangle(width, 0, 5, height));

        if (sliderValues.size() > 1) {
            renderer.setColorRGB(1, 0, 0);
            renderer.drawFilledRect(new Rectangle(currentIndex
                    / (float) (sliderValues.size() - 1) * width, 0, 8, height));
            renderer.setColorRGB(1, 1, 1);

            getManager().getFont(FontType.BUTTON_CAPTION).renderText(renderer,
                    sliderValues.get(currentIndex),
                    new Vector2f(width + 40, height / 1.5f));
        }

        super.draw(renderer);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
        if (new Rectangle(0, 0, 5, height).translate(getAbsolutePosition())
                .containsPoint(e.getPos()) && currentIndex > 0) {
            currentIndex--;
        }

        if (new Rectangle(width, 0, 5, height).translate(getAbsolutePosition())
                .containsPoint(e.getPos())
                && currentIndex < sliderValues.size() - 1) {
            currentIndex++;
        }
    }

}
