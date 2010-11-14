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

import java.util.List;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.gui.components.Button;
import walledin.engine.gui.components.ListView;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.gamemode.GameMode;
import walledin.game.network.ServerData;

public class ServerList extends ListView<ServerData> implements
        ScreenMouseEventListener {
    private static final int MAX_VISIBLE = 17;
    private static final String[] COLUMN_NAMES = { "Name", "Players", "Type" };
    private static final float[] COLUMN_WIDTH = { 150f, 70f, 170f };

    private final AbstractScreen refreshButton;
    private List<ServerData> serverList; // list of servers
    private final ClientLogicManager clientLogicManager;

    public ServerList(final AbstractScreen parent, final Rectangle boudingRect,
            final ClientLogicManager clientLogicManager) {
        super(parent, boudingRect, 1, COLUMN_NAMES.length, COLUMN_NAMES,
                COLUMN_WIDTH, MAX_VISIBLE);
        this.clientLogicManager = clientLogicManager;

        refreshButton = new Button(this, "Refresh", new Vector2f(400, 40));
        refreshButton.addMouseEventListener(this);
        addChild(refreshButton);
        show(); // standard is active and visible
    }

    @Override
    public void update(final double delta) {
        serverList = clientLogicManager.getClient().getServerList();

        /* Start with an empty list. */
        resetData();

        for (final ServerData serverData : serverList) {
            final String[] stringData = { serverData.getName(),
                    serverData.getPlayers() + "/" + serverData.getMaxPlayers(),
                    serverData.getGameMode().toString() };
            addData(new RowData<ServerData>(serverData, stringData));
        }

        sortData();
        updateScrollBar();

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);

        renderer.drawRectOutline(getRectangle());
        super.draw(renderer);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        super.onMouseDown(e);
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
        super.onMouseHover(e);
    }

    @Override
    protected void onListItemClicked(final ServerData item) {
        /* If clicked on server, load the game */
        clientLogicManager.getClient().connectToServer(item.getAddress());

        if (clientLogicManager.getClient().isConnected()) {

            /* If it is a team game, load the team selection screen */
            final GameMode gameMode = item.getGameMode();
            if (gameMode == GameMode.BRIDGE_BUILDER
                    || gameMode == GameMode.TEAM_DEATHMATCH) {
                getManager().getScreen(ScreenType.SELECT_TEAM).initialize();
                getManager().getScreen(ScreenType.SELECT_TEAM).show();
            } else {
                getManager().getScreen(ScreenType.GAME).initialize();
                getManager().getScreen(ScreenType.GAME).show();
            }

            getParent().hide();
        }

        super.onListItemClicked(item);
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
        /* If clicked on refresh button, get server list */
        if (e.getScreen() == refreshButton) {
            getManager().createDialog("Refreshing server list.");

            // request a refresh
            clientLogicManager.getClient().refreshServerList();
        }

        super.onMouseClicked(e);
    }

}
