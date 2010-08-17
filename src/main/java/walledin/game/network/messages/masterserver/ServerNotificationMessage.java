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
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
