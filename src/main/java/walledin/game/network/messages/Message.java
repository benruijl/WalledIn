package walledin.game.network.messages;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public interface Message {
    void write(ByteBuffer buffer);

    void read(ByteBuffer buffer);

    void fireEvent(NetworkEventListener listener, SocketAddress address);
}
