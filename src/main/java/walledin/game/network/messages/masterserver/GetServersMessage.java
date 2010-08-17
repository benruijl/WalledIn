package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class GetServersMessage extends MasterServerMessage {

    public GetServersMessage() {
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_SERVERS_MESSAGE);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
