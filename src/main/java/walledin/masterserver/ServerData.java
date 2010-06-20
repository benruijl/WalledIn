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
package walledin.masterserver;

import java.net.SocketAddress;

public class ServerData {
    private long timeLastSeen;
    private final SocketAddress address;
    private final String name;
    private final int players;
    private final int maxPlayers;

    public ServerData(final SocketAddress address, final String name,
            final int players, final int maxPlayers) {
        this.address = address;
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public long getTimeLastSeen() {
        return timeLastSeen;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setTimeLastSeen(final long timeLastSeen) {
        this.timeLastSeen = timeLastSeen;
    }
}
