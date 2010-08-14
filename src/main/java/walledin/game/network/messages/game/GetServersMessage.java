package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class GetServersMessage extends GameProtocolMessage {

    @Override
    public void read(ByteBuffer buffer) {
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.clear();
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_SERVERS_MESSAGE);
        buffer.flip();
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
