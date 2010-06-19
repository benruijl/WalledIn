package walledin.masterserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MasterServer implements NetworkEventListener {
    private static final int PORT = 1234;
    private static final long TIMEOUT = 2000;
    private boolean running;
    private DatagramChannel channel;
    private final Map<SocketAddress,Server> servers;
    private final NetworkReader networkReader;
    private final NetworkWriter networkWriter;

    public MasterServer() {
        servers = new HashMap<SocketAddress, Server>();
        running = false;
        networkReader = new NetworkReader(this);
        networkWriter = new NetworkWriter();
    }

    public static void main(String[] args) {
        try {
            new MasterServer().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(PORT));
        channel.configureBlocking(false);
        running = true;
        while (running) {
            networkReader.recieveMessage(channel);
            cleanList();
        }
    }

    private void cleanList() {
        long time = System.currentTimeMillis();
        Iterator<Server> it = servers.values().iterator();
        while (it.hasNext()){
            Server server = it.next();
            if (server.getTimeLastSeen() < (time - TIMEOUT)) {
                it.remove();
            }
        }
    }

    @Override
    public void receivedGetServersMessage(SocketAddress address) {
        // TODO: protect against ddos
        networkWriter.sendServersMessage(channel, address,servers.values());
    }

    @Override
    public void receivedServerNotificationMessage(SocketAddress address,
            Server server) {
        // TODO: check if the ip is same as advertised
        server.setTimeLastSeen(System.currentTimeMillis());
        servers.put(server.getAddress(), server);
    }
}
