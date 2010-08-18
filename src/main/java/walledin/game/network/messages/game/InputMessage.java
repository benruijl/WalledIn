package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerAction;
import walledin.game.network.NetworkEventListener;

public class InputMessage extends GameMessage {
    private int version;
    private Set<PlayerAction> playerActions;
    private Vector2f mousePos;

    public InputMessage() {
    }

    public InputMessage(final int version,
            final Set<PlayerAction> playerActions, final Vector2f mousePos) {
        this.version = version;
        this.playerActions = playerActions;
        this.mousePos = mousePos;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        version = buffer.getInt();
        final short numActions = buffer.getShort();
        playerActions = new HashSet<PlayerAction>();
        for (int i = 0; i < numActions; i++) {
            playerActions.add(PlayerAction.values()[buffer.getShort()]);
        }
        mousePos = new Vector2f(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(version);
        buffer.putShort((short) playerActions.size());
        for (final PlayerAction actions : playerActions) {
            buffer.putShort((short) actions.ordinal());
        }
        buffer.putFloat(mousePos.getX());
        buffer.putFloat(mousePos.getY());
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }

    public int getVersion() {
        return version;
    }

    public Set<PlayerAction> getPlayerActions() {
        return playerActions;
    }

    public Vector2f getMousePos() {
        return mousePos;
    }
}
