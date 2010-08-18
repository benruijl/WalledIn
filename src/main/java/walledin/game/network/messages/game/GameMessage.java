package walledin.game.network.messages.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkMessageReader;
import walledin.game.network.messages.NetworkMessage;
import walledin.util.Utils;

public abstract class GameMessage extends NetworkMessage {
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageReader.class);
    private static final int DATAGRAM_IDENTIFICATION = 0x47583454;
    private static final Map<Byte, Class<? extends GameMessage>> MESSAGE_CLASSES = initializeMessageClasses();
    private static final Map<Class<? extends GameMessage>, Byte> MESSAGE_BYTES = Utils
            .reverseMap(MESSAGE_CLASSES);

    private static Map<Byte, Class<? extends GameMessage>> initializeMessageClasses() {
        final Map<Byte, Class<? extends GameMessage>> result = new HashMap<Byte, Class<? extends GameMessage>>();
        result.put((byte) 0, GamestateMessage.class);
        result.put((byte) 1, GetPlayerInfoMessage.class);
        result.put((byte) 2, GetPlayerInfoResponseMessage.class);
        result.put((byte) 3, InputMessage.class);
        result.put((byte) 4, LoginMessage.class);
        result.put((byte) 5, LoginResponseMessage.class);
        result.put((byte) 6, LogoutMessage.class);
        result.put((byte) 7, TeamSelectMessage.class);
        return result;
    }

    public static GameMessage getMessage(final byte type) {
        final Class<? extends GameMessage> clazz = MESSAGE_CLASSES.get(type);
        if (clazz == null) {
            LOG.error("Message of requested type (" + type + ") is unknown");
            return null;
        }
        try {
            return clazz.newInstance();
        } catch (final InstantiationException e) {
            LOG.error("Message of requested class (" + clazz
                    + ") could not be instantiated", e);
            return null;
        } catch (final IllegalAccessException e) {
            LOG.error("Message of requested class (" + clazz
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
