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
package walledin.game.network.messages.masterserver;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkMessageReader;
import walledin.game.network.messages.NetworkMessage;
import walledin.util.Utils;

public abstract class AbstractMasterServerMessage implements NetworkMessage {
    public static final int DATAGRAM_IDENTIFICATION = 0x2C6853CA;
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageReader.class);
    private static final Map<Byte, Class<? extends AbstractMasterServerMessage>> MESSAGE_CLASSES = initializeMessageClasses();
    private static final Map<Class<? extends AbstractMasterServerMessage>, Byte> MESSAGE_BYTES = Utils
            .reverseMap(MESSAGE_CLASSES);

    private static Map<Byte, Class<? extends AbstractMasterServerMessage>> initializeMessageClasses() {
        final Map<Byte, Class<? extends AbstractMasterServerMessage>> result = new HashMap<Byte, Class<? extends AbstractMasterServerMessage>>();
        result.put((byte) 0, GetServersMessage.class);
        result.put((byte) 1, ServerNotificationMessage.class);
        result.put((byte) 2, ServersMessage.class);
        result.put((byte) 3, ChallengeMessage.class);
        return result;
    }

    public static AbstractMasterServerMessage getMessage(final byte type) {
        final Class<? extends AbstractMasterServerMessage> clazz = MESSAGE_CLASSES
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
