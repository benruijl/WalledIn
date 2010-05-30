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

import org.apache.log4j.Logger;

import walledin.game.entity.Entity;

public class PlayerConnection {
	private static final Logger LOG = Logger.getLogger(PlayerConnection.class);
	private final Entity player;
	private final SocketAddress address;

	private final long CHECK_TIME = 1000;
	private final long WAIT_TIME = 1000;
	private long prevTime;
	private boolean waitingForAck;
	private boolean alive;

	public PlayerConnection(final SocketAddress address, final Entity player) {
		super();
		this.player = player;
		this.address = address;

		alive = true;
		prevTime = System.currentTimeMillis();
	}

	public Entity getPlayer() {
		return player;
	}

	public SocketAddress getAddress() {
		return address;
	}

	public void processAliveReceived() {
		waitingForAck = false;
	}

	public boolean getAlive() {
		return alive;
	}

	public void setAlive(final boolean alive) {
		this.alive = alive;
	}

	public void remove() {
		alive = false;
	}

	public boolean update() {
		final long curTime = System.currentTimeMillis();

		if (!alive) {
			return false;
		}

		if (waitingForAck && curTime - prevTime > WAIT_TIME) {
			LOG.info("Connection lost to client " + address.toString());
			alive = false;
			return false;
		}

		if (curTime - prevTime > CHECK_TIME) {
			prevTime = curTime;
			waitingForAck = true;
			return true;

		}
		return false;
	}

}
