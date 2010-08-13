package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.GameMode;
import walledin.game.PlayerActions;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class ServerNotificationResponseMessage extends GameProtocolMessage {
    private int port;
    private String name;
    private int players;
    private int maxPlayers;
    private GameMode gameMode;

    @Override
    public void read(ByteBuffer buffer) {
        final int nameLength = buffer.getInt();
        final byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        name = new String(nameBytes);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.SERVER_NOTIFICATION_MESSAGE);
        buffer.putInt(port);
        writeStringData(name, buffer);
        buffer.putInt(players);
        buffer.putInt(maxPlayers);
        buffer.putInt(gameMode.ordinal());
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
