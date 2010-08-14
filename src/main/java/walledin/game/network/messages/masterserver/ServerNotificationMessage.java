package walledin.game.network.messages.masterserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.GameMode;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;

public class ServerNotificationMessage extends MasterServerProtocolMessage {
    private ServerData server;

    @Override
    public void read(final ByteBuffer buffer) {
        final InetSocketAddress inetAddress = (InetSocketAddress) address;
        // only read port. ip is derived from connection
        final int port = buffer.getInt();
        final SocketAddress serverAddress = new InetSocketAddress(
                InetAddress.getByAddress(inetAddress.getAddress().getAddress()),
                port);
        final String name = readStringData(buffer);
        final int players = buffer.getInt();
        final int maxPlayers = buffer.getInt();
        final GameMode gameMode = GameMode.values()[buffer.getInt()];
        final ServerData server = new ServerData(serverAddress, name, players,
                maxPlayers, gameMode);
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
