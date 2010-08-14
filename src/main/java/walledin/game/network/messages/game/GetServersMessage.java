package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class GetServersMessage extends GameProtocolMessage {

    @Override
    public void read(final ByteBuffer buffer) {
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.clear();
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_SERVERS_MESSAGE);
        buffer.flip();
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
