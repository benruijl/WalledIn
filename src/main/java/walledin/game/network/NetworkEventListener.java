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
package walledin.game.network;

import java.net.SocketAddress;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.PlayerActions;
import walledin.game.entity.Entity;
import walledin.game.Teams;
import walledin.game.network.NetworkConstants.ErrorCodes;

public interface NetworkEventListener {
    boolean receivedGamestateMessage(SocketAddress address, int oldVersion,
            int newVersion);

    void receivedLoginMessage(SocketAddress address, String name);

    void receivedLoginReponseMessage(SocketAddress address,
            ErrorCodes errorCode, String playerEntityName);

    void receivedLogoutMessage(SocketAddress address);

    void receivedInputMessage(SocketAddress address, int newVersion,
            Set<PlayerActions> playerActions, Vector2f cursorPos);

    void receivedChallengeMessage(SocketAddress address, long challengeData);

    void receivedServersMessage(SocketAddress address, Set<ServerData> servers);

    void receivedServerNotificationMessage(SocketAddress address,
            ServerData server);
    
    void entityCreated(Entity entity);

    void receivedGetPlayerInfoMessage(SocketAddress address);

    void receivedGetPlayerInfoResponseMessage(SocketAddress address,
            Set<PlayerClientInfo> players);

    void receivedTeamSelectMessage(SocketAddress address, Teams team);
}
