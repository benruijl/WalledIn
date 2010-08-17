package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkConstants.ErrorCode;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class LoginResponseMessage extends GameMessage {
    private ErrorCode errorCode;
    private String entityName;

    public LoginResponseMessage() {
    }

    public LoginResponseMessage(final ErrorCode errorCode,
            final String entityName) {
        this.errorCode = errorCode;
        this.entityName = entityName;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        errorCode = ErrorCode.values()[buffer.getInt()];
        entityName = NetworkMessageReader.readStringData(buffer);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_RESPONSE_MESSAGE);
        NetworkMessageWriter.writeIntegerData(errorCode.ordinal(), buffer);
        NetworkMessageWriter.writeStringData(entityName, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
