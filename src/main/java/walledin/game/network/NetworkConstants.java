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

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import walledin.util.SettingsManager;

public class NetworkConstants {
    // TODO: what is max upd size?
    public static final int BUFFER_SIZE = 1024 * 1024;

    public static final int MASTER_PROTOCOL_PORT = SettingsManager
            .getInstance().getInteger("network.masterServerPort");
    public static final SocketAddress MASTERSERVER_ADDRESS = new InetSocketAddress(
            SettingsManager.getInstance().getString(
                    "network.masterServerAddress"), MASTER_PROTOCOL_PORT);
    public static final SocketAddress BROADCAST_ADDRESS = new InetSocketAddress(
            "255.255.255.255", MASTER_PROTOCOL_PORT);

    public static String getAddressRepresentation(final SocketAddress address) {
        final InetSocketAddress inetAddr = (InetSocketAddress) address;
        return inetAddr.getAddress().getHostAddress() + "@"
                + inetAddr.getPort();
    }

}
