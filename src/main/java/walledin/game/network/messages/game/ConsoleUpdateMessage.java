package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.messages.NetworkMessage;

public class ConsoleUpdateMessage extends AbstractGameMessage {
    private String message;

    public ConsoleUpdateMessage() {
    }

    public ConsoleUpdateMessage(String message) {
        this.message = message;
    }

    @Override
    public void read(ByteBuffer buffer, SocketAddress address) {
        message = NetworkMessageReader.readStringData(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) {
        NetworkMessageWriter.writeStringData(message, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
    
    public String getMessage() {
        return message;
    }
}
