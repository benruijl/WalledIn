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
package walledin.game.network.messages.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.network.NetworkMessageReader;
import walledin.game.network.messages.NetworkMessage;
import walledin.util.Utils;

public abstract class AbstractGameMessage implements NetworkMessage {
    public static final int DATAGRAM_IDENTIFICATION = 0x47583454;
    private static final Logger LOG = Logger
            .getLogger(NetworkMessageReader.class);
    private static final Map<Byte, Class<? extends AbstractGameMessage>> MESSAGE_CLASSES = initializeMessageClasses();
    private static final Map<Class<? extends AbstractGameMessage>, Byte> MESSAGE_BYTES = Utils
            .reverseMap(MESSAGE_CLASSES);

    private static Map<Byte, Class<? extends AbstractGameMessage>> initializeMessageClasses() {
        final Map<Byte, Class<? extends AbstractGameMessage>> result = new HashMap<Byte, Class<? extends AbstractGameMessage>>();
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

    public static AbstractGameMessage getMessage(final byte type) {
        final Class<? extends AbstractGameMessage> clazz = MESSAGE_CLASSES
                .get(type);
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
