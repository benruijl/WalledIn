package walledin.game.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NetworkConstants {
	public static final int BUFFER_SIZE = 1024 * 1024;
	public static final int DATAGRAM_IDENTIFICATION = 0x47583454;
	public static final byte LOGIN_MESSAGE = 0;
	public static final byte INPUT_MESSAGE = 1;
	public static final byte LOGOUT_MESSAGE = 2;
	public static final byte ALIVE_MESSAGE = 3;
	public static final byte GAMESTATE_MESSAGE = 4;
	public static final byte CREATE_ENTITY = 0;
	public static final byte REMOVE_ENTITY = 1;
	public static final byte UPDATE_ENTITY = 2;
	
	public static String getAddressRepresentation(final SocketAddress address) {
		final InetSocketAddress inetAddr = (InetSocketAddress) address;
		return inetAddr.getAddress().getHostAddress() + "@"
				+ inetAddr.getPort();
	}

}
