package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.Team;
import walledin.game.network.NetworkEventListener;

public class TeamSelectMessage extends GameMessage {
    private Team team;

    public TeamSelectMessage() {
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        team = Team.values()[buffer.getInt()];
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(team.ordinal());
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }

    public Team getTeam() {
        return team;
    }
}
