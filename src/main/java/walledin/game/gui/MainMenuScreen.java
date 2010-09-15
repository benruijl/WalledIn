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
import walledin.engine.TextureManager;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.ScreenManager;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.gui.components.Button;
import walledin.engine.gui.components.ListView;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.util.Utils;

public class MainMenuScreen extends AbstractScreen implements
        ScreenMouseEventListener {
    private AbstractScreen startButton;
    private AbstractScreen quitButton;
    private final ClientLogicManager clientLogicManager;

    public MainMenuScreen(final ScreenManager manager,
            final ClientLogicManager clientLogicManager) {
        super(manager, null, 0);
        this.clientLogicManager = clientLogicManager;
    }

    @Override
    public void draw(final Renderer renderer) {
        super.draw(renderer);

        renderer.drawRect("logo", new Rectangle(250, 50, 256, 128));
    }

    @Override
    public void initialize() {
        TextureManager.getInstance().loadFromURL(
                Utils.getClasspathURL("logo.png"), "logo");

        startButton = new Button(this, "Start game", new Vector2f(330, 200));
        startButton.addMouseEventListener(this);
        quitButton = new Button(this, "Quit", new Vector2f(360, 250));
        quitButton.addMouseEventListener(this);
        addChild(startButton);
        addChild(quitButton);
    }

    @Override
    public void update(final double delta) {
        if (getState() == ScreenState.Hidden) {
            return;
        }

        super.update(delta);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
        if (e.getScreen() == startButton) {
            getManager().getScreen(ScreenType.SERVER_LIST).initialize();
            getManager().getScreen(ScreenType.SERVER_LIST).show();
            hide();
        }

        if (e.getScreen() == quitButton) {
            clientLogicManager.dispose(); // quit application
        }
    }

}
