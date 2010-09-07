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
package walledin.game.gamemode;

import walledin.game.GameLogicManager;
import walledin.game.PlayerInfo;
import walledin.util.SettingsManager;

public class DeathMatch implements GameModeHandler {
    /** The game logic manager. */
    private final GameLogicManager gameLogicManager;
    /** Kill count at which the game is over. */
    private int maxKills;
    /** The current kill count. */
    private int currentMaxKills;

    public DeathMatch(GameLogicManager gameLogicManager) {
        this.gameLogicManager = gameLogicManager;

        maxKills = SettingsManager.getInstance().getInteger(
                "game.deathmatch.maxKills");
        currentMaxKills = 0;
    }

    @Override
    public void update(final double delta) {
        for (PlayerInfo p : gameLogicManager.getPlayers().values()) {
            currentMaxKills = Math.max(currentMaxKills, p.getKillCount());
        }

        if (currentMaxKills >= maxKills) {
            /* The match is over. */
            gameLogicManager.onMatchOver();
        }
    }
}
