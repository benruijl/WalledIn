package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public class GetPlayerInfoMessage extends GameMessage {
    public GetPlayerInfoMessage() {
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
    }

    @Override
    public void write(final ByteBuffer buffer) {
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
