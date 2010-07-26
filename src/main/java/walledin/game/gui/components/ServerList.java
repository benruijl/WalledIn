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
package walledin.game.gui.components;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.gui.Screen;
import walledin.game.gui.ScreenManager.ScreenType;
import walledin.game.gui.ScreenMouseEvent;
import walledin.game.gui.ScreenMouseEventListener;
import walledin.game.network.ServerData;

public class ServerList extends Screen implements ScreenMouseEventListener {
    Screen refreshButton;
    List<ServerData> serverList; // list of servers
    List<Screen> serverButtons; // list of buttons

    public ServerList(final Screen parent, final Rectangle boudingRect) {
        super(parent, boudingRect, 1);
        serverButtons = new ArrayList<Screen>();

        refreshButton = new Button(this, "Refresh", new Vector2f(400, 40));
        refreshButton.addMouseEventListener(this);
        addChild(refreshButton);
        show(); // standard is active and visible
    }

    @Override
    public void update(final double delta) {
        serverList = getManager().getClient().getServerList();

        for (int i = 0; i < serverButtons.size(); i++) {
            removeChild(serverButtons.get(i));
        }

        serverButtons.clear();

        for (int i = 0; i < serverList.size(); i++) {
            final Screen server = new Button(this,
                    serverList.get(i).getName()
                            + " ("
                            + ((InetSocketAddress) serverList.get(i)
                                    .getAddress()).getAddress() + ")" + " "
                            + serverList.get(i).getPlayers() + "/"
                            + serverList.get(i).getMaxPlayers() + " players",
                    new Vector2f(10, 65 + i * 20));
            server.registerScreenManager(getManager());
            server.addMouseEventListener(this);
            serverButtons.add(server);
            addChild(server);
        }

        for (int i = 0; i < serverButtons.size(); i++) {
            serverButtons.get(i).update(delta);
        }

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, "Server Name", new Vector2f(10, 40));

        renderer.drawRectOutline(getRectangle());
        super.draw(renderer);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        /* If clicked on refresh button, get server list */
        if (e.getScreen() == refreshButton) {
            getManager().createDialog("Refreshing server list.");

            // request a refresh
            getManager().getClient().refreshServerList();
            Input.getInstance().setButtonUp(1); // FIXME
        }

        /* If clicked on server, load the game */
        for (int i = 0; i < serverButtons.size(); i++) {
            if (e.getScreen() == serverButtons.get(i)) {
                // connect to server
                getManager().getClient().connectToServer(serverList.get(i));

                if (getManager().getClient().connectedToServer()) {
                    getManager().getScreen(ScreenType.GAME).initialize();
                    getManager().getScreen(ScreenType.GAME).show();
                    getParent().hide();
                }

                Input.getInstance().setButtonUp(1); // FIXME
            }
        }

    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

}
