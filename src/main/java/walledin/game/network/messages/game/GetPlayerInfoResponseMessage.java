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
package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import walledin.game.PlayerClientInfo;
import walledin.game.Team;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class GetPlayerInfoResponseMessage extends GameMessage {
    private Set<PlayerClientInfo> players;

    public GetPlayerInfoResponseMessage() {
    }

    public GetPlayerInfoResponseMessage(
            final Collection<? extends PlayerClientInfo> players) {
        this.players = new HashSet<PlayerClientInfo>(players);
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        players = new HashSet<PlayerClientInfo>();
        final int numPlayers = buffer.getInt();

        for (int i = 0; i < numPlayers; i++) {
            final String entityName = NetworkMessageReader
                    .readStringData(buffer);
            final Team team = Team.values()[buffer.getInt()];
            players.add(new PlayerClientInfo(entityName, team));
        }
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(players.size());

        for (final PlayerClientInfo player : players) {
            NetworkMessageWriter
                    .writeStringData(player.getEntityName(), buffer);
            buffer.putInt(player.getTeam().ordinal());
        }
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }

    public Set<PlayerClientInfo> getPlayers() {
        return players;
    }
}
