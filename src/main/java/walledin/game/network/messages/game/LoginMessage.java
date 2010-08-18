package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class LoginMessage extends GameMessage {
    private String name;

    public LoginMessage() {
    }

    public LoginMessage(final String name) {
        this.name = name;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        name = NetworkMessageReader.readStringData(buffer);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        NetworkMessageWriter.writeStringData(name, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }

    public String getName() {
        return name;
    }
}
