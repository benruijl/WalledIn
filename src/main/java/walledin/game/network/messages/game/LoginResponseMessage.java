package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkConstants.ErrorCodes;

public class LoginResponseMessage extends GameProtocolMessage {
    private ErrorCodes errorMessage;
    private String entityName;

    @Override
    public void read(ByteBuffer buffer) {
        errorMessage = ErrorCodes.values()[buffer.getInt()];
        final int nameLength = buffer.getInt();
        final byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        entityName = new String(nameBytes);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_RESPONSE_MESSAGE);
        writeIntegerData(errorMessage.ordinal(), buffer);
        writeStringData(entityName, buffer);
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
