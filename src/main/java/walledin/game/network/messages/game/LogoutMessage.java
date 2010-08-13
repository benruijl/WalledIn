package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class LogoutMessage extends GameProtocolMessage {
    @Override
    public void read(ByteBuffer buffer) {
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGOUT_MESSAGE);
    }
    
    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
