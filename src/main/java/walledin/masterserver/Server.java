package walledin.masterserver;

import java.net.SocketAddress;

public class Server {
    private long timeLastSeen;
    private final SocketAddress address;
    private final String name;
    private final int players;
    private final int maxPlayers;

    public Server(SocketAddress address, String name, int players,
            int maxPlayers) {
        this.address = address;
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public long getTimeLastSeen() {
        return timeLastSeen;
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

    public void setTimeLastSeen(long timeLastSeen) {
        this.timeLastSeen = timeLastSeen;
    }
}
