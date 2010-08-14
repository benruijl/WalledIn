package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class ChallengeResponseMessage extends MasterServerProtocolMessage {
    private long challengeData;

    @Override
    public void read(ByteBuffer buffer) {
        challengeData = buffer.getLong();
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.CHALLENGE_RESPONSE_MESSAGE);
        buffer.putLong(challengeData);
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
