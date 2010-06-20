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
package walledin.masterserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collection;

public class NetworkWriter {
    private final ByteBuffer buffer;

    public NetworkWriter() {
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
    }

    public void sendServersMessage(final DatagramChannel channel,
            final SocketAddress address, final Collection<ServerData> servers)
            throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.SERVERS_MESSAGE);
        buffer.putInt(servers.size());
        for (final ServerData server : servers) {
            writeServerData(server);
        }
        buffer.flip();
        channel.send(buffer, address);
    }

    public void sendChallengeMessage(final DatagramChannel channel,
            final SocketAddress address, final long challengeData)
            throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.CHALLENGE_MESSAGE);
        buffer.putLong(challengeData);
        buffer.flip();
        channel.send(buffer, address);
    }

    private void writeServerData(final ServerData server) {
        writeInetSocketAddress((InetSocketAddress) server.getAddress());
        writeStringData(server.getName());
        buffer.putInt(server.getPlayers());
        buffer.putInt(server.getMaxPlayers());
    }

    private void writeStringData(final String data) {
        buffer.putInt(data.length());
        buffer.put(data.getBytes());
    }

    private void writeInetSocketAddress(final InetSocketAddress address) {
        buffer.put(address.getAddress().getAddress());
        // write unsigned short
        buffer.putShort((short) (address.getPort() - Short.MIN_VALUE));
    }

}
