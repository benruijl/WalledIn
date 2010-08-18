package walledin.game.network.messages.masterserver;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkMessageReader;
import walledin.game.network.messages.NetworkMessage;
import walledin.util.Utils;

public abstract class MasterServerMessage extends NetworkMessage {
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageReader.class);
    public static final int DATAGRAM_IDENTIFICATION = 0x174BC126;
    private static final Map<Byte, Class<? extends MasterServerMessage>> MESSAGE_CLASSES = initializeMessageClasses();
    private static final Map<Class<? extends MasterServerMessage>, Byte> MESSAGE_BYTES = Utils
            .reverseMap(MESSAGE_CLASSES);

    private static Map<Byte, Class<? extends MasterServerMessage>> initializeMessageClasses() {
        final Map<Byte, Class<? extends MasterServerMessage>> result = new HashMap<Byte, Class<? extends MasterServerMessage>>();
        result.put((byte) 0, GetServersMessage.class);
        result.put((byte) 1, ServerNotificationMessage.class);
        result.put((byte) 2, ServersMessage.class);
        result.put((byte) 3, ChallengeMessage.class);
        return result;
    }

    public static MasterServerMessage getMessage(final byte type) {
        final Class<? extends MasterServerMessage> clazz = MESSAGE_CLASSES
                .get(type);
        try {
            return clazz.newInstance();
        } catch (final InstantiationException e) {
            LOG.error("Message of requested type (" + type
                    + ") could not be instantiated", e);
            return null;
        } catch (final IllegalAccessException e) {
            LOG.error("Message of requested type (" + type
                    + ") could not be instantiated", e);
            return null;
        }
    }

    @Override
    public void writeHeader(final ByteBuffer buffer) {
        buffer.putInt(DATAGRAM_IDENTIFICATION);
        buffer.put(MESSAGE_BYTES.get(getClass()));
    }
}
