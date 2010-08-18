package walledin.game.network.messages;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;

public abstract class NetworkMessage {
    /**
     * Write the message body to the byte buffer. It should be possible to
     * restore the message by calling the read method on the buffer.
     * 
     * @param buffer
     *            The buffer the message is written to.
     */
    public abstract void write(ByteBuffer buffer);

    /**
     * Read the message body from the byte buffer. It should be possible to get
     * the same bytes back when calling the write method.
     * 
     * @param buffer
     *            The buffer the message is read from
     * @param address
     *            The address the message was recieved from.
     */
    public abstract void read(ByteBuffer buffer, SocketAddress address);

    /**
     * Fire the corresponding event.
     * 
     * @param listener
     * @param address
     *            The address the message was recieved from
     */
    public abstract void fireEvent(NetworkEventListener listener,
            SocketAddress address);

    /**
     * Writes the header of the message.
     * 
     * @param buffer
     *            The buffer the header is written to.
     */
    public abstract void writeHeader(ByteBuffer buffer);
}
