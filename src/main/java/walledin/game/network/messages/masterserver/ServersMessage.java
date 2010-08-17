package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.ServerData;

public class ServersMessage extends MasterServerMessage {
    private static final Logger LOG = Logger.getLogger(ServersMessage.class);
    private Set<ServerData> servers;

    public ServersMessage() {
    }

    public Set<ServerData> getServers() {
        return servers;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        final int amount = buffer.getInt();
        servers = new HashSet<ServerData>();
        for (int i = 0; i < amount; i++) {
            ServerData server;
            try {
                server = NetworkMessageReader.readServerData(buffer);
                servers.add(server);
            } catch (final UnknownHostException e) {
                LOG.warn("UnknownHostException when reader server", e);
            }
        }
    }

    @Override
    public void write(final ByteBuffer buffer) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
