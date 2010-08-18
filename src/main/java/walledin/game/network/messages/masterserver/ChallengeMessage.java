package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public class ChallengeMessage extends MasterServerMessage {
    private long challengeData;

    public ChallengeMessage() {
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        challengeData = buffer.getLong();
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putLong(challengeData);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
