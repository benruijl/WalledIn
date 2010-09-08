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
import java.util.HashSet;
import java.util.Set;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerAction;
import walledin.game.network.NetworkEventListener;

public class InputMessage extends AbstractGameMessage {
    private int version;
    private Set<PlayerAction> playerActions;
    private Vector2f mousePos;

    public InputMessage() {
    }

    public InputMessage(final int version,
            final Set<PlayerAction> playerActions, final Vector2f mousePos) {
        this.version = version;
        this.playerActions = playerActions;
        this.mousePos = mousePos;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        version = buffer.getInt();
        final short numActions = buffer.getShort();
        playerActions = new HashSet<PlayerAction>();
        for (int i = 0; i < numActions; i++) {
            playerActions.add(PlayerAction.values()[buffer.getShort()]);
        }
        mousePos = new Vector2f(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(version);
        buffer.putShort((short) playerActions.size());
        for (final PlayerAction actions : playerActions) {
            buffer.putShort((short) actions.ordinal());
        }
        buffer.putFloat(mousePos.getX());
        buffer.putFloat(mousePos.getY());
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }

    public int getVersion() {
        return version;
    }

    public Set<PlayerAction> getPlayerActions() {
        return playerActions;
    }

    public Vector2f getMousePos() {
        return mousePos;
    }
}
