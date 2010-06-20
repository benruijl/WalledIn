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

public class NetworkConstants {
    public static final int BUFFER_SIZE = 1024 * 1024;
    public static final int DATAGRAM_IDENTIFICATION = 0x174BC126;
    public static final byte GET_SERVERS_MESSAGE = 0;
    public static final byte SERVER_NOTIFICATION_MESSAGE = 1;
    public static final byte SERVERS_MESSAGE = 2;
    public static final byte CHALLENGE_RESPONSE_MESSAGE = 3;
    public static final byte CHALLENGE_MESSAGE = 4;
}
