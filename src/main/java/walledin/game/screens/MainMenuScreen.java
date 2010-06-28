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
package walledin.game.screens;

import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.MessageType;
import walledin.game.screens.ScreenManager.ScreenType;
import walledin.util.Utils;

public class MainMenuScreen extends Screen {
    Screen startButton;
    Screen quitButton;

    public MainMenuScreen() {
        super(null, null);
    }

    @Override
    public void draw(final Renderer renderer) {
        super.draw(renderer);
        
        renderer.drawRect("logo", new Rectangle(250, 50, 256, 128));
        
        getManager().getCursor().sendMessage(MessageType.RENDER, renderer);
    }

    @Override
    public void initialize() {
        TextureManager.getInstance().loadFromURL(
                Utils.getClasspathURL("logo.png"), "logo");

        startButton = new Button(this, new Rectangle(0, -20, 100, 25),
                "Start game", new Vector2f(330, 200));
        quitButton = new Button(this, new Rectangle(0, -20, 70, 25),
                "Quit", new Vector2f(360, 250));
        addChild(startButton);
        addChild(quitButton);
    }

    @Override
    public void update(final double delta) {
        if (getState() == ScreenState.Hidden) {
            return;
        }

        super.update(delta);
        
        if (quitButton.pointInScreen(Input.getInstance().getMousePos()
                .asVector2f())) {
            if (Input.getInstance().getMouseDown()) {
            getManager().dispose(); // quit application
            }
        }

        if (startButton.pointInScreen(Input.getInstance().getMousePos()
                .asVector2f())) {
            if (Input.getInstance().getMouseDown()) {
                getManager().getScreen(ScreenType.SERVER_LIST).initialize();
                getManager().getScreen(ScreenType.SERVER_LIST).setActive(true);
                setState(ScreenState.Hidden); // hide main menu
                setActive(false);
                Input.getInstance().setMouseUp(); // FIXME
            }
        }

    }

}
