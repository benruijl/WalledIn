package walledin.game.network;

import java.net.SocketAddress;

public class ServerData {
    private final SocketAddress address;
    private final String name;
    private final int players;
    private final int maxPlayers;

    public ServerData(final SocketAddress address, final String name,
            final int players, final int maxPlayers) {
        this.address = address;
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
