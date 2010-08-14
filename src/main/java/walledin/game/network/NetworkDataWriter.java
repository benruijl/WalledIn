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
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.Team;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.network.messages.game.GameProtocolMessage;

/**
 * Writes network messages
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

    private void writeMessage(final GameProtocolMessage message) {
        buffer.clear();
        message.write(buffer);
        buffer.flip();
    }

    public void sendMessage(final DatagramChannel channel,
            final GameProtocolMessage message) throws IOException {
        writeMessage(message);
        channel.write(buffer);
    }

    public void sendMessage(final DatagramChannel channel,
            final SocketAddress address, final GameProtocolMessage message)
            throws IOException {
        writeMessage(message);
        channel.send(buffer, address);
    }

    public static void writeAttributeData(final Attribute attribute,
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
            writeIntegerData(((Team) data).ordinal(), buffer);
            break;
        case WALLEDIN_IN:
            writeFloatData((Float) data, buffer);
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

    public static void writeAttributesData(final Entity entity,
            final Set<Attribute> attributes, final ByteBuffer buffer) {
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_ATTRIBUTES);
        writeStringData(entity.getName(), buffer);
        final Map<Attribute, Object> values = entity.getAttributes(attributes);
        buffer.putInt(attributes.size());
        for (final Map.Entry<Attribute, Object> entry : values.entrySet()) {
            writeAttributeData(entry.getKey(), entry.getValue(), buffer);
        }
    }

    public static void writeIntegerData(final int data, final ByteBuffer buffer) {
        buffer.putInt(data);
    }

    public static void writeFloatData(final float data, final ByteBuffer buffer) {
        buffer.putFloat(data);
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
}
