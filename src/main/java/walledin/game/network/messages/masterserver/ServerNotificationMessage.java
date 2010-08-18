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
package walledin.game.network.messages.masterserver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.GameMode;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.ServerData;

public class ServerNotificationMessage extends MasterServerMessage {
    private ServerData server;

    public ServerNotificationMessage() {
    }

    public ServerNotificationMessage(final ServerData server) {
        this.server = server;
    }

    public ServerData getServer() {
        return server;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        final InetSocketAddress inetAddress = (InetSocketAddress) address;
        // only read port. ip is derived from connection
        final int port = buffer.getInt();
        final SocketAddress serverAddress = new InetSocketAddress(
                inetAddress.getAddress(), port);
        final String name = NetworkMessageReader.readStringData(buffer);
        final int players = buffer.getInt();
        final int maxPlayers = buffer.getInt();
        final GameMode gameMode = GameMode.values()[buffer.getInt()];
        server = new ServerData(serverAddress, name, players, maxPlayers,
                gameMode);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        // FIXME .... we need this
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
