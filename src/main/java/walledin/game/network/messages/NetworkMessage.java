package walledin.game.network.messages;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public interface NetworkMessage {
    /**
     * Write the message to the byte buffer. It should be possible to restore
     * the message by calling the read method on the buffer.
     * 
     * @param buffer
     *            The buffer the message is written to.
     */
    void write(ByteBuffer buffer);

    /**
     * Read the message from the byte buffer. It should be possible to get the
     * same bytes back when calling the write method.
     * 
     * @param buffer
     *            The buffer the message is read from
     * @param address
     *            The address the message was recieved from.
     */
    void read(ByteBuffer buffer, SocketAddress address);

    /**
     * Fire the corresponding event.
     * 
     * @param listener
     * @param address
     *            The address the message was recieved from
     */
    void fireEvent(NetworkEventListener listener, SocketAddress address);
}
