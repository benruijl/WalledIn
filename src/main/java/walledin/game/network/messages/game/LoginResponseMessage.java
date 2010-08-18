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

import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;

public class LoginResponseMessage extends GameMessage {
    private ErrorCode errorCode;
    private String entityName;
    
    /** Network error code lists. */
    public enum ErrorCode {
        ERROR_SUCCESSFULL,
        ERROR_LOGIN_FAILED,
        ERROR_SERVER_IS_FULL,
        ERROR_ALREADY_LOGGED_IN;
    }

    public LoginResponseMessage() {
    }

    public LoginResponseMessage(final ErrorCode errorCode,
            final String entityName) {
        this.errorCode = errorCode;
        this.entityName = entityName;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        errorCode = ErrorCode.values()[buffer.getInt()];
        entityName = NetworkMessageReader.readStringData(buffer);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        NetworkMessageWriter.writeIntegerData(errorCode.ordinal(), buffer);
        NetworkMessageWriter.writeStringData(entityName, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
