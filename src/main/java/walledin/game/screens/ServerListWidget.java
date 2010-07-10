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

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.network.ServerData;
import walledin.game.screens.ScreenManager.ScreenType;

public class ServerListWidget extends Screen implements
        ScreenMouseEventListener {
    Screen refreshButton;
    List<ServerData> serverList; // list of servers
    List<Screen> serverButtons; // list of buttons

    public ServerListWidget(final Screen parent, final Rectangle boudingRect) {
        super(parent, boudingRect);
        serverButtons = new ArrayList<Screen>();

        setActiveAndVisible(); // standard is active and visible
    }

    @Override
    public void initialize() {
        refreshButton = new Button(this, "Refresh", getPosition().add(
                new Vector2f(400, 40)));
        refreshButton.addMouseEventListener(this);
        addChild(refreshButton);

        // request a refresh of the server list
        getManager().getClient().refreshServerList();
    }

    @Override
    public void update(final double delta) {
        serverList = new ArrayList<ServerData>(getManager().getClient()
                .getServerList());

        serverButtons.clear();

        for (int i = 0; i < serverList.size(); i++) {
            final Screen server = new Button(this, serverList.get(i).getName()
                    + " (" + serverList.get(i).getAddress() + ")",
                    getPosition().add(new Vector2f(10, 65 + i * 20)));
            server.registerScreenManager(getManager());
            server.addMouseEventListener(this);
            serverButtons.add(server);
        }

        for (int i = 0; i < serverButtons.size(); i++) {
            serverButtons.get(i).update(delta);
        }

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, "Server Name",
                getPosition().add(new Vector2f(10, 40)));

        for (int i = 0; i < serverButtons.size(); i++) {
            serverButtons.get(i).draw(renderer);
        }

        // TODO Auto-generated method stub
        renderer.drawRectOutline(getRectangle().translate(getPosition()));
        super.draw(renderer);
    }

    @Override
    public void onMouseDown(ScreenMouseEvent e) {
        /* If clicked on refresh button, get server list */
        if (e.getScreen() == refreshButton) {
            serverButtons.clear();

            getManager().createDialog("Refreshing server list.");
          /*  PopupDialog dialog = new PopupDialog(this, "Refreshing server list");
            getManager().addScreen(ScreenType.DIALOG, dialog);
            dialog.initialize();
            dialog.popUp();*/

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
                    getManager().getScreen(ScreenType.GAME).setActive(true);
                    getParent().setState(ScreenState.Hidden); // hide main menu
                    getParent().setActive(false);
                }

                Input.getInstance().setButtonUp(1); // FIXME
            }
        }

    }

    @Override
    public void onMouseHover(ScreenMouseEvent e) {
    }

}
