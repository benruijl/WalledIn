package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerActions;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class InputMessage extends GameProtocolMessage {
    private int version;
    private Set<PlayerActions> playerActions;
    private Vector2f mousePos;

    @Override
    public void read(ByteBuffer buffer) {
        version = buffer.getInt();
        final short numActions = buffer.getShort();
        playerActions = new HashSet<PlayerActions>();
        for (int i = 0; i < numActions; i++) {
            playerActions.add(PlayerActions.values()[buffer.getShort()]);
        }
        mousePos = new Vector2f(buffer.getFloat(),
                buffer.getFloat());
    }
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.INPUT_MESSAGE);
        buffer.putInt(version);
        buffer.putShort((short) playerActions.size());
        for (final PlayerActions actions : playerActions) {
            buffer.putShort((short) actions.ordinal());
        }
        buffer.putFloat(mousePos.getX());
        buffer.putFloat(mousePos.getY());
    }
    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
