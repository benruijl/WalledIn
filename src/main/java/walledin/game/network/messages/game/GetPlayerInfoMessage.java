package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerActions;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class GetPlayerInfoMessage extends GameProtocolMessage {

    @Override
    public void read(ByteBuffer buffer) {
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_PLAYER_INFO_MESSAGE);
    }
    
    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
