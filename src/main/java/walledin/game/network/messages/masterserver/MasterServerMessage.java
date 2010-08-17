package walledin.game.network.messages.masterserver;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkMessageReader;
import walledin.game.network.messages.NetworkMessage;

public abstract class MasterServerMessage implements NetworkMessage {
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageReader.class);
    private static final Map<Byte, Class<? extends MasterServerMessage>> MESSAGE_CLASSES = initializeMessageClasses();

    private static Map<Byte, Class<? extends MasterServerMessage>> initializeMessageClasses() {
        final Map<Byte, Class<? extends MasterServerMessage>> result = new HashMap<Byte, Class<? extends MasterServerMessage>>();
        result.put((byte) 0, ChallengeResponseMessage.class);
        result.put((byte) 1, GetServersMessage.class);
        result.put((byte) 2, ServerNotificationMessage.class);
        result.put((byte) 3, ServerNotificationResponseMessage.class);
        result.put((byte) 4, ServersMessage.class);
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
}
