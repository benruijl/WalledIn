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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.GameMode;
import walledin.game.Team;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.util.Utils;

/**
 * Reads network messages
 * 
 * The receiveMessage method reads the current datagram in the channel and
 * processes it. The methods with process prefix process each type of message.
 * The methods with read prefix read the data into a object or into the entity.
 * 
 * @author Wouter Smeenk
 * 
 */
public class NetworkMessageReader {
    private static final Logger LOG = Logger.getLogger(NetworkMessageReader.class);
    private final ByteBuffer buffer;
    private final NetworkEventListener listener;

    public NetworkMessageReader(final NetworkEventListener listener) {
        this.listener = listener;
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
    }

    public static ServerData readServerData() throws UnknownHostException {
        final byte[] ip = new byte[4];
        buffer.get(ip);
        final int port = buffer.getInt();
        final SocketAddress serverAddress = new InetSocketAddress(
                InetAddress.getByAddress(ip), port);
        final String name = readStringData(buffer);
        final int players = buffer.getInt();
        final int maxPlayers = buffer.getInt();
        final GameMode gameMode = GameMode.values()[buffer.getInt()];
        return new ServerData(serverAddress, name, players, maxPlayers,
                gameMode);
    }

    public static void readAttributeData(final Entity entity,
            final ByteBuffer buffer, final EntityManager entityManager) {
        // Write attribute identification
        final short ord = buffer.getShort();
        // FIXME don't use ordinal
        final Attribute attribute = Attribute.values()[ord];
        Object data = null;
        switch (attribute) {
        case HEIGHT:
            data = buffer.getInt();
            break;
        case WIDTH:
            data = buffer.getInt();
            break;
        case HEALTH:
            data = buffer.getInt();
            break;
        case ORIENTATION_ANGLE:
            data = buffer.getFloat();
            break;
        case PLAYER_NAME:
            data = readStringData(buffer);
            break;
        case PLAYER_TEAM:
            data = Team.values()[buffer.getInt()];
            break;
        case WALLEDIN_IN:
            data = buffer.getFloat();
            break;
        case POSITION:
            data = readVector2fData(buffer);
            break;
        case VELOCITY:
            data = readVector2fData(buffer);
            break;
        case TILE_WIDTH:
            data = buffer.getFloat();
            break;
        default:
            LOG.error("Could not process attribute " + attribute);
            break;
        }
        entity.setAttribute(attribute, data);
    }

    public static void readAttributesData(final Entity entity,
            final ByteBuffer buffer, final EntityManager entityManager) {
        final int num = buffer.getInt();
        for (int i = 0; i < num; i++) {
            readAttributeData(entity, buffer, entityManager);
        }
    }

    /**
     * Read data for a entity.
     * 
     * @param entityManager
     * @param buffer
     * @return true if there is more in the list
     */
    public static boolean readEntityData(final EntityManager entityManager,
            final ByteBuffer buffer) {
        final int type = buffer.get();
        if (type == NetworkConstants.GAMESTATE_MESSAGE_END) {
            return false;
        }
        final String name = readStringData(buffer);
        Entity entity = null;
        switch (type) {
        case NetworkConstants.GAMESTATE_MESSAGE_CREATE_ENTITY:
            final String familyName = readStringData(buffer);
            final Family family = Enum.valueOf(Family.class, familyName);

            entity = entityManager.create(family, name);
            readFamilySpecificData(family, entity);
            listener.entityCreated(entity);
            break;
        case NetworkConstants.GAMESTATE_MESSAGE_REMOVE_ENTITY:
            entityManager.remove(name);
            break;
        case NetworkConstants.GAMESTATE_MESSAGE_ATTRIBUTES:
            entity = entityManager.get(name);
            readAttributesData(entity, buffer, entityManager);
            break;
        }
        return true;
    }

    public static void readFamilySpecificData(final Family family,
            final Entity entity) {
        switch (family) {
        case MAP:
            final String mapName = readStringData(buffer);
            entity.setAttribute(Attribute.MAP_NAME, mapName);

            // load the tiles
            final GameMapIO mapIO = new GameMapIOXML();
            entity.setAttribute(Attribute.TILES,
                    mapIO.readTilesFromURL(Utils.getClasspathURL(mapName)));
            break;
        default:
            break;
        }
    }

    public static Object readEntityListData(final ByteBuffer buffer,
            final EntityManager entityManager) {
        final int size = buffer.getInt();
        final List<Entity> entities = new ArrayList<Entity>();
        for (int i = 0; i < size; i++) {
            final String name = readStringData(buffer);
            final Entity entity = entityManager.get(name);
            entities.add(entity);
        }
        return entities;
    }

    public static String readStringData(final ByteBuffer buffer) {
        final int size = buffer.getInt();
        final byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new String(bytes);
    }

    public static Object readVector2fData(final ByteBuffer buffer) {
        final float x = buffer.getFloat();
        final float y = buffer.getFloat();
        return new Vector2f(x, y);
    }

    /**
     * Reads a datagram from the channel if there is one.
     * 
     * @param channel
     *            The channel to read from
     * @return the source address if a datagram was present on the channel, else
     *         null
     * @throws IOException
     */
    public SocketAddress readMessage(final DatagramChannel channel)
            throws IOException {
        buffer.clear();
        final SocketAddress address = channel.receive(buffer);
        buffer.flip();
        return address;
    }

    /**
     * Processes the message in the buffer
     * 
     * @param entityManager
     *            the entity manager to process the changes in
     * @throws UnknownHostException
     */
    public void processMessage(final SocketAddress address,
            final EntityManager entityManager) throws UnknownHostException {
        int ident = -1;
        ident = buffer.getInt();
        if (ident == NetworkConstants.DATAGRAM_IDENTIFICATION) {
            final byte type = buffer.get();
            switch (type) {
            case NetworkConstants.GAMESTATE_MESSAGE:
                processGamestateMessage(entityManager, address);
                break;
            case NetworkConstants.LOGIN_MESSAGE:
                processLoginMessage(address);
                break;
            case NetworkConstants.LOGIN_RESPONSE_MESSAGE:
                processLoginResponseMessage(address);
                break;
            case NetworkConstants.LOGOUT_MESSAGE:
                processLogoutMessage(address);
                break;
            case NetworkConstants.INPUT_MESSAGE:
                processInputMessage(address);
                break;
            case NetworkConstants.GET_PLAYER_INFO_MESSAGE:
                processGetPlayerInfoMessage(address);
                break;
            case NetworkConstants.GET_PLAYER_INFO_RESPONSE_MESSAGE:
                processGetPlayerInfoResponseMessage(address);
                break;
            case NetworkConstants.TEAM_SELECT_MESSAGE:
                processTeamSelectMessage(address);
                break;
            default:
                LOG.warn("Received unhandled message");
                break;
            }
        } else if (ident == NetworkConstants.MS_DATAGRAM_IDENTIFICATION) {
            final byte type = buffer.get();
            switch (type) {
            case NetworkConstants.CHALLENGE_MESSAGE:
                processChallengeMessage(address);
                break;
            case NetworkConstants.SERVERS_MESSAGE:
                processServersMessage(address);
                break;
            case NetworkConstants.SERVER_NOTIFICATION_MESSAGE:
                processServerNotificationMessage(address);
                break;
            default:
                LOG.warn("Received unhandled message");
                break;
            }
        } else {
            LOG.warn("Unknown ident");
            // else ignore the datagram, incorrect format
        }
    }
}
