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
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

public class MasterServer implements NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(MasterServer.class);
    private static final int PORT = 1235;
    private static final long TIMEOUT = 2000;
    private static final long CHALLENGE_INTERVAL = 1000;
    private boolean running;
    private DatagramChannel channel;
    private final Map<SocketAddress, ServerData> servers;
    private final NetworkReader networkReader;
    private final NetworkWriter networkWriter;
    private long challengeData;
    private final Random random;

    public MasterServer() {
        servers = new HashMap<SocketAddress, ServerData>();
        running = false;
        networkReader = new NetworkReader(this);
        networkWriter = new NetworkWriter();
        random = new Random();
    }

    public static void main(final String[] args) {
        try {
            new MasterServer().run();
        } catch (final Exception e) {
            LOG.fatal("Exception: ", e);
        }
    }

    public void run() throws IOException, InterruptedException {
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(PORT));
        channel.configureBlocking(false);
        running = true;
        long lastChallenge = System.currentTimeMillis();
        while (running) {
            networkReader.recieveMessage(channel);
            cleanList();
            if (lastChallenge < System.currentTimeMillis() - CHALLENGE_INTERVAL) {
                lastChallenge = System.currentTimeMillis();
                sendChallenge();
            }
            // TODO: can we do blocking with timeout?
            Thread.sleep(1);
        }
    }

    private void sendChallenge() throws IOException {
        challengeData = random.nextLong();
        // TODO: hash with server ip?
        for (final ServerData server : servers.values()) {
            networkWriter.sendChallengeMessage(channel, server.getAddress(),
                    challengeData);
        }
    }

    private void cleanList() {
        final long time = System.currentTimeMillis();
        final Iterator<ServerData> it = servers.values().iterator();
        while (it.hasNext()) {
            final ServerData server = it.next();
            if (server.getTimeLastSeen() < time - TIMEOUT) {
                it.remove();
                LOG.info("Removed server: " + server);
            }
        }
    }

    @Override
    public void receivedGetServersMessage(final SocketAddress address) {
        LOG.info("Request for servers by " + address);
        // TODO: protect against ddos
        try {
            networkWriter
                    .sendServersMessage(channel, address, servers.values());
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
        }
    }

    @Override
    public void receivedServerNotificationMessage(final SocketAddress address,
            final ServerData server) {
        LOG.info("Server notification from " + address + " server: " + server);
        server.setTimeLastSeen(System.currentTimeMillis());
        final ServerData knownServer = servers.get(server.getAddress());
        if (knownServer != null) {
            // If we already have this server keep the old time last seen. this
            // should only be updated by challenge response
            server.setTimeLastSeen(knownServer.getTimeLastSeen());
        }
        servers.put(server.getAddress(), server);
    }

    @Override
    public void receivedChallengeMessage(final SocketAddress address,
            final long challengeData) {
        final ServerData server = servers.get(address);
        if (server != null && this.challengeData == challengeData) {
            server.setTimeLastSeen(System.currentTimeMillis());
        }
    }
}
