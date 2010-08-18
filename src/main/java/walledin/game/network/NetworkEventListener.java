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

import walledin.game.entity.Entity;
import walledin.game.network.messages.game.GamestateMessage;
import walledin.game.network.messages.game.GetPlayerInfoMessage;
import walledin.game.network.messages.game.GetPlayerInfoResponseMessage;
import walledin.game.network.messages.game.InputMessage;
import walledin.game.network.messages.game.LoginMessage;
import walledin.game.network.messages.game.LoginResponseMessage;
import walledin.game.network.messages.game.LogoutMessage;
import walledin.game.network.messages.game.TeamSelectMessage;
import walledin.game.network.messages.masterserver.ChallengeMessage;
import walledin.game.network.messages.masterserver.GetServersMessage;
import walledin.game.network.messages.masterserver.ServerNotificationMessage;
import walledin.game.network.messages.masterserver.ServersMessage;

public interface NetworkEventListener {

    void receivedMessage(SocketAddress address, LoginMessage loginMessage);

    void entityCreated(Entity entity);

    void receivedMessage(SocketAddress address, ChallengeMessage message);

    void receivedMessage(SocketAddress address, GetPlayerInfoMessage message);

    void receivedMessage(SocketAddress address, GamestateMessage message);

    void receivedMessage(SocketAddress address, TeamSelectMessage message);

    void receivedMessage(SocketAddress address, ServersMessage message);

    void receivedMessage(SocketAddress address,
            ServerNotificationMessage message);

    void receivedMessage(SocketAddress address, LogoutMessage message);

    void receivedMessage(SocketAddress address, LoginResponseMessage message);

    void receivedMessage(SocketAddress address, InputMessage message);

    void receivedMessage(SocketAddress address, GetServersMessage message);

    void receivedMessage(SocketAddress address,
            GetPlayerInfoResponseMessage message);
}
