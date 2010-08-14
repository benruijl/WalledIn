package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;

public class ServersMessage extends MasterServerProtocolMessage {
    private int amount;
    private Set<ServerData> servers;

    @Override
    public void read(ByteBuffer buffer) {
        amount = buffer.getInt();
        servers = new HashSet<ServerData>();
        for (int i = 0; i < amount; i++) {
            final ServerData server = readServerData();
            servers.add(server);
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
