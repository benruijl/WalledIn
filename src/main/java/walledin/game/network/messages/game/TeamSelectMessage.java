package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.Teams;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class TeamSelectMessage extends GameProtocolMessage {
    private Teams team;

    @Override
    public void read(ByteBuffer buffer) {
        team = Teams.values()[buffer.getInt()];
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.TEAM_SELECT_MESSAGE);
        buffer.putInt(team.ordinal());
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
