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

import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.ScreenKeyEvent;
import walledin.engine.gui.ScreenKeyEventListener;
import walledin.engine.gui.ScreenManager;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.gui.components.TextBox;
import walledin.engine.input.Input;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.gui.components.ServerList;

public class ServerListScreen extends AbstractScreen implements
        ScreenKeyEventListener {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ServerListScreen.class);
    private final AbstractScreen serverListWidget;
    private final TextBox ipWidget;
    private final ClientLogicManager clientLogicManager;

    public ServerListScreen(final ScreenManager manager,
            final ClientLogicManager clientLogicManager) {
        super(manager, null, 0);
        addKeyEventListener(this);
        this.clientLogicManager = clientLogicManager;

        ipWidget = new TextBox(this, "localhost", new Vector2f(100, 450), 230, 20);
        addChild(ipWidget);
        serverListWidget = new ServerList(this, new Rectangle(0, 0, 500, 400),
                clientLogicManager);
        serverListWidget.setPosition(new Vector2f(100, 0));
        addChild(serverListWidget);
    }

    @Override
    public void update(final double delta) {
        super.update(delta);
    }

    @Override
    protected void onVisibilityChanged(final boolean visible) {
        if (visible) {
            clientLogicManager.getClient().bindServerNotifyChannel();

            // request a refresh of the server list
            clientLogicManager.getClient().refreshServerList();
        } else {
            clientLogicManager.getClient().unbindServerNotifyChannel();
        }
        super.onVisibilityChanged(visible);
    }

    @Override
    public void draw(final Renderer renderer) {
        super.draw(renderer);
    }

    @Override
    public void onKeyDown(final ScreenKeyEvent e) {
        if (e.getKeys().contains(KeyEvent.VK_ESCAPE)) {
            Input.getInstance().setKeyUp(KeyEvent.VK_ESCAPE);

            /*
             * If playing a game, return to it when pressing escape. Otherwise,
             * return to main menu.
             */
            if (clientLogicManager.getClient().isConnected()) {
                getManager().getScreen(ScreenType.GAME).show();
            } else {
                getManager().getScreen(ScreenType.MAIN_MENU).show();
            }

            hide();
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onKeyUp(ScreenKeyEvent e) {
        // TODO Auto-generated method stub
        
    }

}
