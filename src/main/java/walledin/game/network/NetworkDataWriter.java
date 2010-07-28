/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.game.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.PlayerActions;
import walledin.game.Teams;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants.ErrorCodes;
import walledin.game.network.server.ChangeSet;

/**
 * Writes network messages
 * 
 * All the methods with send prefix write a complete message and reset the
 * buffer. Methods with write prefix write part of a message and assume there is
 * enough room in the buffer.
 * 
 * @author Wouter Smeenk
 * 
 */
public class NetworkDataWriter {
    private static final Logger LOG = Logger.getLogger(NetworkDataWriter.class);
    private final ByteBuffer buffer;

    public NetworkDataWriter() {
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
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

    public void prepareGamestateMessage(final EntityManager entityManager,
            final ChangeSet changeSet, final int knownClientVersion,
            final int currentVersion) {
        buffer.clear();
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
        buffer.flip();
        buffer.rewind();
    }

    public void prepareInputMessage(final int version,
            final Set<PlayerActions> playerActions, final Vector2f mousePos) {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.INPUT_MESSAGE);
        buffer.putInt(version);
        buffer.putShort((short) playerActions.size());
        for (final PlayerActions actions : playerActions) {
            buffer.putShort((short) actions.ordinal());
        }
        buffer.putFloat(mousePos.getX());
        buffer.putFloat(mousePos.getY());
        buffer.flip();
    }

    public void prepareLoginMessage(final String username) {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_MESSAGE);
        buffer.putInt(username.length());
        buffer.put(username.getBytes());
        buffer.flip();
    }

    public void prepareLoginResponseMessage(final ErrorCodes errorMessage,
            final String entityName) {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_RESPONSE_MESSAGE);
        writeIntegerData(errorMessage.ordinal(), buffer);
        writeStringData(entityName, buffer);
        buffer.flip();
    }

    public void prepareLogoutMessage() {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGOUT_MESSAGE);
        buffer.flip();
    }

    public void prepareGetServersMessage() {
        buffer.clear();
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_SERVERS_MESSAGE);
        buffer.flip();
    }
    
    public void prepareGetPlayerInfoMessage() {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GET_PLAYER_INFO_MESSAGE);
        buffer.flip();
    }

    public void prepareChallengeResponse(final long challengeData) {
        buffer.clear();
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.CHALLENGE_RESPONSE_MESSAGE);
        buffer.putLong(challengeData);
        buffer.flip();
    }

    public void prepareServerNotificationResponse(final int port,
            final String name, final int players, final int maxPlayers) {
        buffer.clear();
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.SERVER_NOTIFICATION_MESSAGE);
        buffer.putInt(port);
        writeStringData(name, buffer);
        buffer.putInt(players);
        buffer.putInt(maxPlayers);
        buffer.flip();
    }

    public void sendBuffer(final DatagramChannel channel) throws IOException {
        channel.write(buffer);
    }

    public void sendBuffer(final DatagramChannel channel,
            final SocketAddress address) throws IOException {
        channel.send(buffer, address);
    }

    private void writeAttributeData(final Attribute attribute,
            final Object data, final ByteBuffer buffer) {
        // Write attribute identification
        buffer.putShort((short) attribute.ordinal());
        switch (attribute) {
        case HEIGHT:
            writeIntegerData((Integer) data, buffer);
            break;
        case WIDTH:
            writeIntegerData((Integer) data, buffer);
            break;
        case HEALTH:
            writeIntegerData((Integer) data, buffer);
            break;
        case ORIENTATION_ANGLE:
            writeFloatData((Float) data, buffer);
            break;
        case TILE_WIDTH:
            writeFloatData((Float) data, buffer);
            break;
        case PLAYER_NAME:
            writeStringData((String) data, buffer);
            break;
        case PLAYER_TEAM:
            writeIntegerData(((Teams) data).ordinal(), buffer);
            break;
        case POSITION:
            writeVector2fData((Vector2f) data, buffer);
            break;
        case VELOCITY:
            writeVector2fData((Vector2f) data, buffer);
            break;
        default:
            LOG.error("Could not process attribute " + attribute);
            break;
        }
    }

    private void writeAttributesData(final Entity entity,
            final Set<Attribute> attributes, final ByteBuffer buffer) {
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_ATTRIBUTES);
        writeStringData(entity.getName(), buffer);
        final Map<Attribute, Object> values = entity.getAttributes(attributes);
        buffer.putInt(attributes.size());
        for (final Map.Entry<Attribute, Object> entry : values.entrySet()) {
            writeAttributeData(entry.getKey(), entry.getValue(), buffer);
        }
    }

    private void writeIntegerData(final int data, final ByteBuffer buffer) {
        buffer.putInt(data);
    }

    private void writeFloatData(final float data, final ByteBuffer buffer) {
        buffer.putFloat(data);
    }

    private void writeStringData(final String data, final ByteBuffer buffer) {
        buffer.putInt(data.length());
        buffer.put(data.getBytes());
    }

    private void writeVector2fData(final Vector2f data, final ByteBuffer buffer) {
        buffer.putFloat(data.getX());
        buffer.putFloat(data.getY());
    }
}
