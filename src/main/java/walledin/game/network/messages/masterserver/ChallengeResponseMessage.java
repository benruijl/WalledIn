package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerActions;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.messages.game.GameProtocolMessage;

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
