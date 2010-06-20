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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.map.Tile;
import walledin.game.map.TileType;

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
public class NetworkDataReader {
    private static final Logger LOG = Logger.getLogger(NetworkDataReader.class);
    private final ByteBuffer buffer;
    private final NetworkEventListener listener;

    public NetworkDataReader(final NetworkEventListener listener) {
        this.listener = listener;
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
    }

    private void processGamestateMessage(final EntityManager entityManager,
            final SocketAddress address) throws IOException {
        final int oldVersion = buffer.getInt();
        final int newVersion = buffer.getInt();
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

    private void processInputMessage(final SocketAddress address) {
        final int newVersion = buffer.getInt();
        final short numKeys = buffer.getShort();
        final Set<Integer> keys = new HashSet<Integer>();
        for (int i = 0; i < numKeys; i++) {
            keys.add((int) buffer.getShort());
        }
        final Vector2f mousePos = new Vector2f(buffer.getFloat(),
                buffer.getFloat());
        final Boolean mouseDown = buffer.getInt() != 0;
        listener.receivedInputMessage(address, newVersion, keys, mousePos,
                mouseDown);
    }

    private void processLoginMessage(final SocketAddress address) {
        final int nameLength = buffer.getInt();
        final byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        final String name = new String(nameBytes);
        listener.receivedLoginMessage(address, name);
    }

    private void processLogoutMessage(final SocketAddress address) {
        listener.receivedLogoutMessage(address);
    }
    
    private void processServersMessage(SocketAddress address) throws UnknownHostException {
        int amount = buffer.getInt();
        Set<ServerData> servers = new HashSet<ServerData>();
        for (int i= 0; i < amount; i++) {
            ServerData server = readServerData();
            servers.add(server);
        }
        listener.receivedServersMessage(address, servers);
    }

    private void processChallengeMessage(SocketAddress address) {
        long challengeData = buffer.getLong();
        listener.receivedChallengeMessage(address,challengeData);
    }
    
    private ServerData readServerData() throws UnknownHostException {
        byte[] ip = new byte[4];
        buffer.get(ip);
        final int port = buffer.getInt();
        final SocketAddress serverAddress = new InetSocketAddress(
                InetAddress.getByAddress(ip), port);
        final String name = readStringData(buffer);
        final int players = buffer.getInt();
        final int maxPlayers = buffer.getInt();
        return new ServerData(serverAddress, name, players, maxPlayers);
    }

    private void readAttributeData(final Entity entity,
            final ByteBuffer buffer, final EntityManager entityManager) {
        // Write attribute identification
        final short ord = buffer.getShort();
        // FIXME don't user ordinal
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
        case ORIENTATION:
            data = buffer.getInt();
            break;
        case PLAYER_NAME:
            data = readStringData(buffer);
            break;
        case ITEM_LIST:
            data = readEntityListData(buffer, entityManager);
            break;
        case TILES:
            data = readTileListData(buffer);
            break;
        case POSITION:
            data = readVector2fData(buffer);
            break;
        case VELOCITY:
            data = readVector2fData(buffer);
            break;
        default:
            LOG.error("Could not process attribute " + attribute);
            break;
        }
        entity.setAttribute(attribute, data);
    }

    private void readAttributesData(final Entity entity,
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
    private boolean readEntityData(final EntityManager entityManager,
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
            entityManager.create(Enum.valueOf(Family.class, familyName), name);
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

    private Object readEntityListData(final ByteBuffer buffer,
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

    private String readStringData(final ByteBuffer buffer) {
        final int size = buffer.getInt();
        final byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new String(bytes);
    }

    private Object readTileListData(final ByteBuffer buffer) {
        final int size = buffer.getInt();
        final List<Tile> tiles = new ArrayList<Tile>();
        for (int i = 0; i < size; i++) {
            final int x = buffer.getInt();
            final int y = buffer.getInt();
            final int ord = buffer.getInt();
            final TileType type = TileType.values()[ord];
            final Tile tile = new Tile(type, x, y);
            tiles.add(tile);
        }
        return tiles;
    }

    private Object readVector2fData(final ByteBuffer buffer) {
        final float x = buffer.getFloat();
        final float y = buffer.getFloat();
        return new Vector2f(x, y);
    }

    /**
     * Reads a datagram from the channel if there is one
     * 
     * @param channel
     *            The channel to read from
     * @param entityManager
     *            the entity manager to process the changes in
     * @return true if a datagram was present on the channel, else false
     * @throws IOException
     */
    public boolean recieveMessage(final DatagramChannel channel,
            final EntityManager entityManager) throws IOException {
        int ident = -1;
        buffer.clear();
        final SocketAddress address = channel.receive(buffer);
        if (address == null) {
            return false;
        }
        buffer.flip();
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
            case NetworkConstants.LOGOUT_MESSAGE:
                processLogoutMessage(address);
                break;
            case NetworkConstants.INPUT_MESSAGE:
                processInputMessage(address);
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
            default:
                LOG.warn("Received unhandled message");
                break;
            }
        } else {
            LOG.warn("Unknown ident");
            // else ignore the datagram, incorrect format
        }
        return true;
    }
}
