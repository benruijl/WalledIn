package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkConstants.ErrorCodes;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class LoginResponseMessage extends MasterServerProtocolMessage {
    private ErrorCodes errorMessage;
    private String entityName;

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        errorMessage = ErrorCodes.values()[buffer.getInt()];
        entityName = NetworkMessageReader.readStringData(buffer);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_RESPONSE_MESSAGE);
        NetworkMessageWriter.writeIntegerData(errorMessage.ordinal(), buffer);
        NetworkMessageWriter.writeStringData(entityName, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
