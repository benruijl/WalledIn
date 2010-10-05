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
import walledin.game.entity.Attribute;
import walledin.game.entity.Family;
import walledin.game.network.messages.NetworkMessage;
import walledin.game.network.server.ChangeSet;

/**
 * Writes network messages
 * 
 * @author Wouter Smeenk
 * 
 */
public class NetworkMessageWriter {
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageWriter.class);
    private final ByteBuffer buffer;
    /** Amount of bytes written so far */
    private long bytesWritten;
    /** Amount of messages written so far */
    private int messagesWritten;

    public NetworkMessageWriter() {
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
        resetStatistics();
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public int getMessagesWritten() {
        return messagesWritten;
    }

    public void resetStatistics() {
        bytesWritten = 0;
        messagesWritten = 0;
    }

    private void writeMessage(final NetworkMessage message) {
        buffer.clear();
        message.writeHeader(buffer);
        message.write(buffer);
        buffer.flip();
        messagesWritten++;
        bytesWritten += buffer.limit();
    }

    public void sendMessage(final DatagramChannel channel,
            final NetworkMessage message) throws IOException {
        writeMessage(message);
        channel.write(buffer);
    }

    public void sendMessage(final DatagramChannel channel,
            final SocketAddress address, final NetworkMessage message)
            throws IOException {
        writeMessage(message);
        channel.send(buffer, address);
    }

    public static void writeAttributeData(final Attribute attribute,
            final Object data, final ByteBuffer buffer) {
        // Write attribute identification
        buffer.putShort((short) attribute.ordinal());
        if (data instanceof Integer) {
            buffer.putInt((Integer) data);
        } else if (data instanceof Float) {
            buffer.putFloat((Float) data);
        } else if (data instanceof String) {
            writeStringData((String) data, buffer);
        } else if (data instanceof Vector2f) {
            writeVector2fData((Vector2f) data, buffer);
        } else {
            LOG.error("Could not process attribute " + attribute
                    + " with data of class " + data.getClass());
        }
    }

    public static void writeChangeSet(final ChangeSet changeSet,
            final ByteBuffer buffer) {
        buffer.putInt(changeSet.getVersion());
        // Write the size of the changeset
        buffer.putInt(changeSet.getRemoved().size());
        buffer.putInt(changeSet.getCreated().size());
        buffer.putInt(changeSet.getUpdated().size());
        for (final Set<String> removed : changeSet.getRemoved()) {
            buffer.putInt(removed.size());
            for (final String name : removed) {
                NetworkMessageWriter.writeStringData(name, buffer);
            }
        }
        for (final Map<String, Family> created : changeSet.getCreated()) {
            buffer.putInt(created.size());
            for (final Entry<String, Family> entry : created.entrySet()) {
                // write name of entity
                writeStringData(entry.getKey(), buffer);
                // write family of entity
                writeFamilyData(entry.getValue(), buffer);
            }
        }
        for (final Entry<String, Map<Attribute, Object>> entry : changeSet
                .getUpdated().entrySet()) {
            // write name of entity
            writeStringData(entry.getKey(), buffer);
            writeAttributes(entry.getValue(), buffer);
        }
    }

    private static void writeAttributes(
            final Map<Attribute, Object> attributes, final ByteBuffer buffer) {
        buffer.putInt(attributes.size());
        for (final Map.Entry<Attribute, Object> attributeEntry : attributes
                .entrySet()) {
            writeAttributeData(attributeEntry.getKey(),
                    attributeEntry.getValue(), buffer);
        }
    }

    private static void writeFamilyData(final Family family,
            final ByteBuffer buffer) {
        writeStringData(family.toString(), buffer);
    }

    public static void writeStringData(final String data,
            final ByteBuffer buffer) {
        buffer.putInt(data.length());
        buffer.put(data.getBytes());
    }

    public static void writeVector2fData(final Vector2f data,
            final ByteBuffer buffer) {
        buffer.putFloat(data.getX());
        buffer.putFloat(data.getY());
    }

    public static void writeServerData(final ServerData server,
            final ByteBuffer buffer) {
        // TODO check size? not ipv6 safe?
        buffer.put(server.getAddress().getAddress().getAddress());
        buffer.putInt(server.getAddress().getPort());
        writeStringData(server.getName(), buffer);
        buffer.putInt(server.getPlayers());
        buffer.putInt(server.getMaxPlayers());
        buffer.putInt(server.getGameMode().ordinal());
    }
}
