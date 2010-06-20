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
