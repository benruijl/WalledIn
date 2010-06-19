package walledin.masterserver;

import java.nio.channels.DatagramChannel;

public class NetworkReader {

    private final NetworkEventListener listener;

    public NetworkReader(NetworkEventListener listener) {
        this.listener = listener;
    }

    public void recieveMessage(DatagramChannel channel) {
        
    }
}
