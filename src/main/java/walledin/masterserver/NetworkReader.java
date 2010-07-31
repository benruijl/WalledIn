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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.Logger;

public class NetworkReader {
    private static final Logger LOG = Logger.getLogger(NetworkReader.class);
    private final NetworkEventListener listener;
    private final ByteBuffer buffer;

    public NetworkReader(final NetworkEventListener listener) {
        this.listener = listener;
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
    }

    public boolean recieveMessage(final DatagramChannel channel)
            throws IOException {
        int ident = -1;
        buffer.clear();
        final SocketAddress address = channel.receive(buffer);
        if (address == null) {
            return false;
        }
        buffer.flip();
        ident = buffer.getInt();
        if (ident != NetworkConstants.DATAGRAM_IDENTIFICATION) {
            // ignore the datagram, incorrect format
            return true;
        }
        final byte type = buffer.get();
        switch (type) {
        case NetworkConstants.GET_SERVERS_MESSAGE:
            processGetServersMessage(address);
            break;
        case NetworkConstants.SERVER_NOTIFICATION_MESSAGE:
            processServerNotificationMessage(address);
            break;
        case NetworkConstants.CHALLENGE_RESPONSE_MESSAGE:
            processChallengeResponseMessage(address);
            break;
        default:
            LOG.warn("Received unhandled message");
            break;
        }
        return true;
    }

    private void processChallengeResponseMessage(final SocketAddress address) {
        final long challengeData = buffer.getLong();
        listener.receivedChallengeResponseMessage(address, challengeData);
    }

    private void processServerNotificationMessage(final SocketAddress address)
            throws UnknownHostException {
        final InetSocketAddress inetAddress = (InetSocketAddress) address;
        // only read port. ip is derived from connection
        final int port = buffer.getInt();
        final SocketAddress serverAddress = new InetSocketAddress(
                InetAddress.getByAddress(inetAddress.getAddress().getAddress()),
                port);
        final String name = readStringData();
        final int players = buffer.getInt();
        final int maxPlayers = buffer.getInt();
        final int gameMode = buffer.getInt();
        final ServerData server = new ServerData(serverAddress, name, players,
                maxPlayers, gameMode);
        listener.receivedServerNotificationMessage(address, server);
    }

    private void processGetServersMessage(final SocketAddress address) {
        listener.receivedGetServersMessage(address);
    }

    private String readStringData() {
        final int size = buffer.getInt();
        final byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new String(bytes);
    }
}
