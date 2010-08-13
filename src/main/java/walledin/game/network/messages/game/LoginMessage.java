package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class LoginMessage extends GameProtocolMessage {
    private String name;

    @Override
    public void read(ByteBuffer buffer) {
        final int nameLength = buffer.getInt();
        final byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        name = new String(nameBytes);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_MESSAGE);
        buffer.putInt(name.length());
        buffer.put(name.getBytes());
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
