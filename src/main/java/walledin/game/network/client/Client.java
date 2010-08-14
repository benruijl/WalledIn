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
package walledin.game.network.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.audio.Audio;
import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.GameLogicManager;
import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.PlayerAction;
import walledin.game.PlayerActionManager;
import walledin.game.Team;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkConstants.ErrorCodes;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;
import walledin.util.SettingsManager;

public final class Client implements NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private SocketAddress host;
    private String username;
    private final NetworkMessageWriter networkDataWriter;
    private final NetworkMessageReader networkDataReader;
    private final DatagramChannel channel;
    private final DatagramChannel masterServerChannel;
    private DatagramChannel serverNotifyChannel;

    /* Data that may be requested by player. */
    /** List of servers from the master server. */
    private Set<ServerData> internetServerList;
    /** List of servers on LAN. */
    private final Set<ServerData> lanServerList;
    /** List of players of the current server */
    private final Set<PlayerClientInfo> playerList;

    /** Keeps track if the player is connected to a server. */
    private boolean connected = false;
    private boolean connectedMasterServer = false;
    private boolean boundServerNotifyChannel = false;
    private int receivedVersion = 0;
    private long lastLoginTry;
    /** The time when the last update was received. */
    private long lastUpdate;
    private final long LOGIN_RETRY_TIME;
    private final long TIME_OUT_TIME;
    /** The renderer. */
    private final Renderer renderer;
    private final ClientLogicManager clientLogicManager;

    /**
     * Create the client.
     * 
     * @param renderer
     *            Current renderer
     * @throws IOException
     */
    public Client(final Renderer renderer,
            final ClientLogicManager clientLogicManager) throws IOException {
        this.renderer = renderer;
        this.clientLogicManager = clientLogicManager;

        networkDataWriter = new NetworkMessageWriter();
        networkDataReader = new NetworkMessageReader(this);
        internetServerList = new HashSet<ServerData>();
        lanServerList = new HashSet<ServerData>();
        playerList = new HashSet<GameLogicManager.PlayerClientInfo>();

        channel = DatagramChannel.open();
        masterServerChannel = DatagramChannel.open();

        LOGIN_RETRY_TIME = SettingsManager.getInstance().getInteger(
                "network.loginRetryTime");

        TIME_OUT_TIME = SettingsManager.getInstance().getInteger(
                "network.timeOutTime");
    }

    public void refreshServerList() {

        if (!connectedMasterServer) {
            return;
        }

        try {
            networkDataWriter.prepareGetServersMessage();
            networkDataWriter.sendBuffer(masterServerChannel);
            lanServerList.clear();
        } catch (final IOException e) {
            LOG.error("IOException", e);
        }
    }

    public List<ServerData> getServerList() {
        final List<ServerData> servers = new ArrayList<ServerData>(
                lanServerList);
        servers.addAll(internetServerList);
        return servers;
    }

    @Override
    public void entityCreated(final Entity entity) {
        // TODO: move to a better place
        /* Play a sound when a bullet is created */
        final Random generator = new Random();
        final int num = generator.nextInt(4) + 1;

        if (Audio.getInstance().isEnabled()) {
            if (entity.getFamily() == Family.HANDGUN_BULLET) {
                Audio.getInstance().playSample("handgun" + num, new Vector2f(),
                        false);
            }

            if (entity.getFamily() == Family.FOAMGUN_BULLET) {
                Audio.getInstance().playSample("foamgun" + num, new Vector2f(),
                        false);
            }
        }
    }

    /**
     * Called when the gamestate has been updated. We only send a new input when
     * we receive the net game state.
     */
    @Override
    public boolean receivedGamestateMessage(final SocketAddress address,
            final int oldVersion, final int newVersion) {
        lastLoginTry = -1;
        boolean result = false;
        if (LOG.isTraceEnabled()) {
            LOG.trace("version:" + newVersion + " receivedVersion:"
                    + receivedVersion + " oldversion: " + oldVersion);
        }
        if (receivedVersion == oldVersion && newVersion > receivedVersion) {
            receivedVersion = newVersion;
            result = true;
        }
        try {
            networkDataWriter.prepareInputMessage(receivedVersion,
                    PlayerActionManager.getInstance().getPlayerActions(),
                    renderer.screenToWorld(Input.getInstance().getMousePos()));
            networkDataWriter.sendBuffer(channel);
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
            dispose();
        }
        return result;
    }

    @Override
    public void receivedServersMessage(final SocketAddress address,
            final Set<ServerData> servers) {
        LOG.info("Received server list. " + servers.size()
                + " servers available.");
        internetServerList = servers;
    }

    @Override
    public void receivedLoginMessage(final SocketAddress address,
            final String name) {
        // ignore
    }

    @Override
    public void receivedLogoutMessage(final SocketAddress address) {
        // ignore
    }

    @Override
    public void receivedInputMessage(final SocketAddress address,
            final int newVersion, final Set<PlayerAction> playerActions,
            final Vector2f cursorPos) {
        // ignore
    }

    @Override
    public void receivedChallengeMessage(final SocketAddress address,
            final long challengeData) {
        // ignore
    }

    @Override
    public void receivedLoginReponseMessage(final SocketAddress address,
            final ErrorCodes errorCode, final String playerEntityName) {

        if (errorCode == ErrorCodes.ERROR_SUCCESSFULL) {
            clientLogicManager.setPlayerName(playerEntityName);
            LOG.info("Player entity name received: " + playerEntityName);
            return;
        }

        switch (errorCode) {
        case ERROR_SERVER_IS_FULL:
            clientLogicManager.displayErrorAndDisconnect("The server is full.");
            break;
        default:
            clientLogicManager
                    .displayErrorAndDisconnect("Could not login to the server.");
            break;
        }
    }

    @Override
    public void receivedServerNotificationMessage(final SocketAddress address,
            final ServerData server) {
        lanServerList.add(server);
    }

    /**
     * Update the current game state.
     * 
     * @param delta
     *            time since last update in seconds
     */
    public void update(final double delta) {
        // network stuff
        try {
            if (connected) {

                if (lastLoginTry >= 0
                        && System.currentTimeMillis() - lastLoginTry > LOGIN_RETRY_TIME) {
                    lastLoginTry = System.currentTimeMillis();
                    networkDataWriter.prepareLoginMessage(username);
                    networkDataWriter.sendBuffer(channel);
                }
                // Read messages.
                SocketAddress address = networkDataReader.readMessage(channel);

                /* If the address is null, no message is received. */
                if (address == null) {
                    if (System.currentTimeMillis() - lastUpdate > TIME_OUT_TIME) {
                        clientLogicManager
                                .displayErrorAndDisconnect("The connection timed out.");
                    }

                } else {
                    lastUpdate = System.currentTimeMillis();
                }

                while (address != null) {
                    networkDataReader.processMessage(address,
                            clientLogicManager.getEntityManager());
                    address = networkDataReader.readMessage(channel);
                }
            }
            if (connectedMasterServer) {
                // Read messages.
                SocketAddress address = networkDataReader
                        .readMessage(masterServerChannel);
                while (address != null) {
                    networkDataReader.processMessage(address,
                            clientLogicManager.getEntityManager());
                    address = networkDataReader
                            .readMessage(masterServerChannel);
                }
            }
            if (boundServerNotifyChannel) {
                SocketAddress address = networkDataReader
                        .readMessage(serverNotifyChannel);
                while (address != null) {
                    networkDataReader.processMessage(address,
                            clientLogicManager.getEntityManager());
                    address = networkDataReader
                            .readMessage(serverNotifyChannel);
                }
            }
        } catch (final PortUnreachableException e) {
            clientLogicManager
                    .displayErrorAndDisconnect("Connection to server lost.");
            LOG.fatal("The port is unreachable.");
        } catch (final IOException e) {
            clientLogicManager
                    .displayErrorAndDisconnect("Connection to server lost.");
        }

    }

    /**
     * Initialize game.
     */
    public void initialize() {
        connectToMasterServer();
    }

    /**
     * Bind the server notify channel so we can receive lan broadcasts.
     */
    public void bindServerNotifyChannel() {
        try {
            serverNotifyChannel = DatagramChannel.open();
            serverNotifyChannel.socket()
                    .bind(new InetSocketAddress(
                            NetworkConstants.MASTER_PROTOCOL_PORT));
            serverNotifyChannel.configureBlocking(false);
            boundServerNotifyChannel = true;
        } catch (final IOException e) {
            LOG.warn("IOException", e);
        }
    }

    /**
     * Unbind the server notify channel.
     */
    public void unbindServerNotifyChannel() {
        try {
            serverNotifyChannel.close();
            boundServerNotifyChannel = false;
        } catch (final IOException e) {
            LOG.warn("IOException", e);
        }
    }

    /**
     * Connects to a game server. If already connected to a server, it will
     * disconnect if the server is a different one. If the server is the same,
     * it will do nothing.
     */
    public void connectToServer(final ServerData server) {
        if (channel.socket().getRemoteSocketAddress() == server.getAddress()) {
            return;
        }

        try {
            lastLoginTry = System.currentTimeMillis();

            LOG.info("Connecting to server " + server.getAddress());
            host = server.getAddress();
            username = System.getProperty("user.name");

            // always try to disconnect. Does nothing if not connected
            channel.disconnect();
            channel.configureBlocking(false);
            channel.connect(host);

            // set the last update to this timed
            lastUpdate = System.currentTimeMillis();

            // the client is connected now
            connected = true;
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            clientLogicManager.getScreenManager().createDialog(
                    "Could not connect to server.");
        }
    }

    public void disconnectFromServer() throws IOException {
        connected = false;
        channel.disconnect();
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Connects to a master server.
     */
    public void connectToMasterServer() {

        /* Connect once */
        if (connectedMasterServer) {
            return;
        }

        LOG.info("configure network channel and connecting to master server");
        try {
            masterServerChannel.configureBlocking(false);
            masterServerChannel.connect(NetworkConstants.MASTERSERVER_ADDRESS);
            connectedMasterServer = true;
        } catch (final PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            clientLogicManager.getScreenManager().createDialog(
                    "Could not connect to master server.");
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            clientLogicManager.getScreenManager().createDialog(
                    "Could not connect to master server.");
        }
    }

    public void dispose() {
        if (connected) {
            try {
                networkDataWriter.prepareLogoutMessage();
                networkDataWriter.sendBuffer(channel);
                connected = false;
            } catch (final IOException e) {
                LOG.fatal("IOException during logout", e);
            }
        }
    }

    @Override
    public void receivedGetPlayerInfoResponseMessage(
            final SocketAddress address, final Set<PlayerClientInfo> players) {
        playerList.clear();
        playerList.addAll(players);
    }

    @Override
    public void receivedGetPlayerInfoMessage(final SocketAddress address) {
        // ignore

    }

    @Override
    public void receivedTeamSelectMessage(final SocketAddress address,
            final Team team) {
        // ignore
    }

    /**
     * Gets the list of players from the current server.
     * 
     * @return List of players
     */
    public Set<PlayerClientInfo> getPlayerList() {
        return playerList;
    }

    /**
     * Refreshes the player list by asking the server for an update.
     */
    public void refreshPlayerList() {
        if (connected) {
            try {
                networkDataWriter.prepareGetPlayerInfoMessage();
                networkDataWriter.sendBuffer(channel);
            } catch (final IOException e) {
                LOG.error("IOException", e);
            }
        }
    }

    public void selectTeam(final Team team) {
        networkDataWriter.prepareTeamSelectMessage(team);
        try {
            networkDataWriter.sendBuffer(channel);
        } catch (final IOException e) {
            LOG.error("IOException", e);
        }
    }
}
