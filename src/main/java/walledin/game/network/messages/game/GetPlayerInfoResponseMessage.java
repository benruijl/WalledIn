package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;

import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.GameLogicManager.PlayerInfo;
import walledin.game.Teams;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;

public class GetPlayerInfoResponseMessage extends GameProtocolMessage {
    private Collection<PlayerInfo> players;

    @Override
    public void read(ByteBuffer buffer) {
        players = new HashSet<PlayerClientInfo>();
        final int numPlayers = buffer.getInt();

        for (int i = 0; i < numPlayers; i++) {
            final String entityName = readStringData(buffer);
            final Teams team = Teams.values()[buffer.getInt()];
            players.add(new PlayerClientInfo(entityName, team));
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_PLAYER_INFO_RESPONSE_MESSAGE);
        buffer.putInt(players.size());

        for (final PlayerInfo player : players) {
            writeStringData(player.getPlayer().getName(), buffer);
            buffer.putInt(player.getTeam().ordinal());
        }
    }

    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
