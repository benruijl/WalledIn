package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;

import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.GameLogicManager.PlayerInfo;
import walledin.game.Team;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class GetPlayerInfoResponseMessage extends GameProtocolMessage {
    private Collection<PlayerInfo> players;

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        players = new HashSet<PlayerClientInfo>();
        final int numPlayers = buffer.getInt();

        for (int i = 0; i < numPlayers; i++) {
            final String entityName = NetworkMessageReader
                    .readStringData(buffer);
            final Team team = Team.values()[buffer.getInt()];
            players.add(new PlayerClientInfo(entityName, team));
        }
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_PLAYER_INFO_RESPONSE_MESSAGE);
        buffer.putInt(players.size());

        for (final PlayerInfo player : players) {
            NetworkMessageWriter.writeStringData(player.getPlayer().getName(),
                    buffer);
            buffer.putInt(player.getTeam().ordinal());
        }
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
