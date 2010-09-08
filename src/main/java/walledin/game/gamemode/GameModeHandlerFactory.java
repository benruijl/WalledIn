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

import org.apache.log4j.Logger;

import walledin.game.GameLogicManager;

public final class GameModeHandlerFactory {
    /** Logger. */
    private static final Logger LOG = Logger
            .getLogger(GameModeHandlerFactory.class);

    /**
     * Class should not be created
     */
    private GameModeHandlerFactory() {
    }

    /**
     * Creates and returns a new game mode handler.
     * 
     * @param mode
     *            Requested mode
     * @param gameLogicManager
     *            The game logic manager
     * 
     * @return Game mode handler
     */
    public static GameModeHandler createHandler(final GameMode mode,
            final GameLogicManager gameLogicManager) {
        switch (mode) {
        case DEATHMATCH:
            return new DeathMatch(gameLogicManager);
        default:
            LOG.warn("Unimplemented gamemode requested.");
            return null;
        }
    }
}
