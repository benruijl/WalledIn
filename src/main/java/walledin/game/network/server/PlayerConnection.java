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
package walledin.game.network.server;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.PlayerAction;
import walledin.game.entity.Entity;

public class PlayerConnection {
    private static final Logger LOG = Logger.getLogger(PlayerConnection.class);

    private final Entity player;
    private Set<PlayerAction> playerActions;
    private Vector2f mousePos;

    private final SocketAddress address;
    private int receivedVersion;
    private boolean isNew;
    private boolean isPlayerActive;

    public PlayerConnection(final SocketAddress address, final Entity player,
            final int currentVersion) {
        super();

        playerActions = new HashSet<PlayerAction>();
        mousePos = new Vector2f();

        this.player = player;
        this.address = address;
        receivedVersion = currentVersion;
        isNew = true;
    }

    public Entity getPlayer() {
        return player;
    }

    public void setPlayerActive(final boolean isPlayerActive) {
        this.isPlayerActive = isPlayerActive;
    }

    public boolean isPlayerActive() {
        return isPlayerActive;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setReceivedVersion(final int receivedVersion) {
        this.receivedVersion = receivedVersion;
    }

    public int getReceivedVersion() {
        return receivedVersion;
    }

    public void setNew() {
        isNew = false;
    }

    public boolean isNew() {
        return isNew;
    }

    public Set<PlayerAction> getPlayerActions() {
        return playerActions;
    }

    public void setPlayerActions(final Set<PlayerAction> playerActions) {
        this.playerActions = playerActions;
    }

    public Vector2f getMousePos() {
        return mousePos;
    }

    public void setMousePos(final Vector2f mousePos) {
        this.mousePos = mousePos;
    }

}
