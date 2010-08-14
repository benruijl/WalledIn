package walledin.game.network.messages;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public interface NetworkMessage {
    void write(ByteBuffer buffer);

    void read(ByteBuffer buffer, SocketAddress address);

    void fireEvent(NetworkEventListener listener, SocketAddress address);
}
