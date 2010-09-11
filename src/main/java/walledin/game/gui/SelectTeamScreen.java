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

import java.util.HashSet;
import java.util.Set;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenManager;
import walledin.engine.gui.ScreenManager.ScreenType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.gui.components.Button;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.PlayerClientInfo;
import walledin.game.Team;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.util.Utils;

public class SelectTeamScreen extends AbstractScreen implements
        ScreenMouseEventListener {
    private final AbstractScreen teamBlue;
    private final AbstractScreen teamRed;
    private final AbstractScreen teamUndefined;
    private final AbstractScreen back;
    private Set<PlayerClientInfo> players;
    private final ClientLogicManager clientLogicManager;

    public SelectTeamScreen(final ScreenManager manager,
            final ClientLogicManager clientLogicManager) {
        super(manager, null, 0);
        players = new HashSet<PlayerClientInfo>();
        this.clientLogicManager = clientLogicManager;

        TextureManager.getInstance().loadFromURL(
                Utils.getClasspathURL("logo.png"), "logo");

        teamBlue = new Button(this, "Team Blue", new Vector2f(200, 200));
        teamBlue.addMouseEventListener(this);
        teamRed = new Button(this, "Team Red", new Vector2f(400, 200));
        teamRed.addMouseEventListener(this);
        teamUndefined = new Button(this, "Spectators", new Vector2f(600, 200));
        back = new Button(this, "Back", new Vector2f(200, 400));
        back.addMouseEventListener(this);
        addChild(teamBlue);
        addChild(teamRed);
        addChild(teamUndefined);
        addChild(back);
    }

    @Override
    public void draw(final Renderer renderer) {
        super.draw(renderer);

        renderer.drawRect("logo", new Rectangle(250, 50, 256, 128));
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);

        /* Output the player names under the correct team */
        int redCount = 0;
        int blueCount = 0;
        int specCount = 0;
        for (final PlayerClientInfo player : players) {
            final Entity playerEntity = clientLogicManager.getEntityManager()
                    .get(player.getEntityName());

            if (playerEntity == null) {
                return;
            }

            switch (player.getTeam()) {
            case BLUE:
                font.renderText(renderer, (String) playerEntity
                        .getAttribute(Attribute.PLAYER_NAME), new Vector2f(200,
                        220 + blueCount * 20));
                blueCount++;
                break;
            case RED:
                font.renderText(renderer, (String) playerEntity
                        .getAttribute(Attribute.PLAYER_NAME), new Vector2f(400,
                        220 + redCount * 20));
                redCount++;
                break;
            case UNSELECTED:
                font.renderText(renderer, (String) playerEntity
                        .getAttribute(Attribute.PLAYER_NAME), new Vector2f(600,
                        220 + specCount * 20));
                specCount++;
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        if (e.getScreen() == teamBlue) {
            clientLogicManager.getClient().selectTeam(Team.BLUE);
            getManager().getScreen(ScreenType.GAME).initialize();
            getManager().getScreen(ScreenType.GAME).show();
            hide();
        }

        if (e.getScreen() == teamRed) {
            clientLogicManager.getClient().selectTeam(Team.RED);
            getManager().getScreen(ScreenType.GAME).initialize();
            getManager().getScreen(ScreenType.GAME).show();
            hide();
        }
        if (e.getScreen() == back) {
            getManager().getScreen(ScreenType.SERVER_LIST).show();
            hide();
            Input.getInstance().setButtonUp(1); // FIXME
        }
    }

    @Override
    public void update(final double delta) {
        clientLogicManager.getClient().refreshPlayerList();
        players = clientLogicManager.getClient().getPlayerList();

        super.update(delta);
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

}
