package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.Team;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class TeamSelectMessage extends GameProtocolMessage {
    private Team team;

    @Override
    public void read(final ByteBuffer buffer) {
        team = Team.values()[buffer.getInt()];
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.TEAM_SELECT_MESSAGE);
        buffer.putInt(team.ordinal());
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
