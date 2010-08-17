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

    // game protocol constants
    public static final int DATAGRAM_IDENTIFICATION = 0x47583454;
    public static final byte LOGIN_MESSAGE = 0;
    public static final byte LOGIN_RESPONSE_MESSAGE = 5;
    public static final byte INPUT_MESSAGE = 1;
    public static final byte LOGOUT_MESSAGE = 2;
    public static final byte GET_PLAYER_INFO_MESSAGE = 3;
    public static final byte GET_PLAYER_INFO_RESPONSE_MESSAGE = 6;
    public static final byte TEAM_SELECT_MESSAGE = 7;
    public static final byte GAMESTATE_MESSAGE = 4;
    public static final byte GAMESTATE_MESSAGE_CREATE_ENTITY = 0;
    public static final byte GAMESTATE_MESSAGE_REMOVE_ENTITY = 1;
    public static final byte GAMESTATE_MESSAGE_ATTRIBUTES = 2;
    public static final byte GAMESTATE_MESSAGE_END = 3;

    /** Network error code lists. */
    public enum ErrorCode {
        ERROR_SUCCESSFULL,
        ERROR_LOGIN_FAILED,
        ERROR_SERVER_IS_FULL,
        ERROR_ALREADY_LOGGED_IN;
    }

    // Master server protocol constants
    public static final int MS_DATAGRAM_IDENTIFICATION = 0x174BC126;
    public static final byte GET_SERVERS_MESSAGE = 0;
    public static final byte SERVER_NOTIFICATION_MESSAGE = 1;
    public static final byte SERVERS_MESSAGE = 2;
    public static final byte CHALLENGE_RESPONSE_MESSAGE = 3;
    public static final byte CHALLENGE_MESSAGE = 4;

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
