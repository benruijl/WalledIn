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

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.gui.components.Button;
import walledin.engine.input.Input;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.gamemode.GameMode;
import walledin.game.network.ServerData;

public class ServerList extends AbstractScreen implements
        ScreenMouseEventListener {
    private final AbstractScreen refreshButton;
    private List<ServerData> serverList; // list of servers
    private final List<AbstractScreen> serverButtons; // list of buttons
    private final ClientLogicManager clientLogicManager;

    public ServerList(final AbstractScreen parent, final Rectangle boudingRect,
            final ClientLogicManager clientLogicManager) {
        super(parent, boudingRect, 1);
        this.clientLogicManager = clientLogicManager;
        serverButtons = new ArrayList<AbstractScreen>();

        refreshButton = new Button(this, "Refresh", new Vector2f(400, 40));
        refreshButton.addMouseEventListener(this);
        addChild(refreshButton);
        show(); // standard is active and visible
    }

    @Override
    public void update(final double delta) {
        serverList = clientLogicManager.getClient().getServerList();

        for (int i = 0; i < serverButtons.size(); i++) {
            removeChild(serverButtons.get(i));
        }

        serverButtons.clear();

        for (int i = 0; i < serverList.size(); i++) {
            final AbstractScreen server = new Button(this, serverList.get(i)
                    .getName()
                    + " ("
                    + serverList.get(i).getAddress().getAddress()
                    + ")"
                    + " "
                    + serverList.get(i).getPlayers()
                    + "/"
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
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);
        font.renderText(renderer, "Server Name", new Vector2f(10, 40));

        renderer.drawRectOutline(getRectangle());
        super.draw(renderer);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseClicked(ScreenMouseEvent e) {
        /* If clicked on refresh button, get server list */
        if (e.getScreen() == refreshButton) {
            getManager().createDialog("Refreshing server list.");

            // request a refresh
            clientLogicManager.getClient().refreshServerList();
        }

        /* If clicked on server, load the game */
        for (int i = 0; i < serverButtons.size(); i++) {
            if (e.getScreen() == serverButtons.get(i)) {
                // connect to server
                clientLogicManager.getClient().connectToServer(
                        serverList.get(i));

                if (clientLogicManager.getClient().isConnected()) {

                    /* If it is a team game, load the team selection screen */
                    final GameMode gameMode = serverList.get(i).getGameMode();
                    if (gameMode == GameMode.BRIDGE_BUILDER
                            || gameMode == GameMode.TEAM_DEATHMATCH) {
                        getManager().getScreen(ScreenType.SELECT_TEAM)
                                .initialize();
                        getManager().getScreen(ScreenType.SELECT_TEAM).show();
                    } else {
                        getManager().getScreen(ScreenType.GAME).initialize();
                        getManager().getScreen(ScreenType.GAME).show();
                    }

                    getParent().hide();
                }
            }
        }
    }

}
