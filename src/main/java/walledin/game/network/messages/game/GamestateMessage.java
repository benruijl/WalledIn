package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import walledin.game.EntityManager;
import walledin.game.GameLogicManager.PlayerInfo;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.server.ChangeSet;

public class GamestateMessage {
    private  ChangeSet changeSet;
    private int knownClientVersion;
    private int currentVersion;
    
    @Override
    public void read(ByteBuffer buffer) {
        knownClientVersion = buffer.getInt();
        currentVersion = buffer.getInt();
        // Ask the client if the we should process this gamestate
        final boolean process = listener.receivedGamestateMessage(address,
                oldVersion, newVersion);
        boolean hasMore = true;
        if (process) {
            while (hasMore) {
                hasMore = readEntityData(entityManager, buffer);
            }
        }
    }

    /**
     * Sometimes it is required to send extra data on entity creation. This
     * function takes care of that.
     * 
     * @param family
     *            Family of entity
     * @param entity
     *            Entity
     */
    private void writeFamilySpecificData(final Family family,
            final Entity entity) {
        switch (family) {
        case MAP:
            writeStringData((String) entity.getAttribute(Attribute.MAP_NAME),
                    buffer);
            break;
        default:
            break;
        }
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE);
        buffer.putInt(knownClientVersion);
        buffer.putInt(currentVersion);
        for (final String name : changeSet.getRemoved()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_REMOVE_ENTITY);
            writeStringData(name, buffer);
        }

        for (final Entry<String, Family> entry : changeSet.getCreated()
                .entrySet()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_CREATE_ENTITY);
            // write name of entity
            writeStringData(entry.getKey(), buffer);
            // write family of entity
            writeStringData(entry.getValue().toString(), buffer);

            // write family specific data
            writeFamilySpecificData(entry.getValue(),
                    entityManager.get(entry.getKey()));
        }

        for (final Entry<String, Set<Attribute>> entry : changeSet.getUpdated()
                .entrySet()) {
            final Entity entity = entityManager.get(entry.getKey());

            writeAttributesData(entity, entry.getValue(), buffer);
        }
        // write end
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_END);
    }
    
    @Override
    public void fireEvent(NetworkEventListener listener, SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
