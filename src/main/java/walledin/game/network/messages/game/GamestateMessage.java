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

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Set;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.server.ChangeSet;

public class GamestateMessage extends GameMessage {
    private ChangeSet changeSet;
    private int knownClientVersion;
    private int currentVersion;

    public GamestateMessage() {
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public int getKnownClientVersion() {
        return knownClientVersion;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
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
    public void write(final ByteBuffer buffer) {
        buffer.putInt(knownClientVersion);
        buffer.putInt(currentVersion);
        for (final String name : changeSet.getRemoved()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_REMOVE_ENTITY);
            NetworkMessageWriter.writeStringData(name, buffer);
        }

        for (final Entry<String, Family> entry : changeSet.getCreated()
                .entrySet()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_CREATE_ENTITY);
            // write name of entity
            NetworkMessageWriter.writeStringData(entry.getKey(), buffer);
            // write family of entity
            NetworkMessageWriter.writeStringData(entry.getValue().toString(),
                    buffer);

            // write family specific data
            writeFamilySpecificData(entry.getValue(),
                    entityManager.get(entry.getKey()));
        }

        for (final Entry<String, Set<Attribute>> entry : changeSet.getUpdated()
                .entrySet()) {
            final Entity entity = entityManager.get(entry.getKey());

            NetworkMessageWriter.writeAttributesData(entity, entry.getValue(),
                    buffer);
        }
        // write end
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_END);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
