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
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.engine.input.Input;
import walledin.game.ClientLogicManager;
import walledin.game.PlayerActionManager;
import walledin.game.PlayerClientInfo;
import walledin.game.Team;
import walledin.game.gamemode.GameMode;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.ServerData;
import walledin.game.network.messages.game.ConsoleUpdateMessage;
import walledin.game.network.messages.game.GamestateMessage;
import walledin.game.network.messages.game.GetPlayerInfoMessage;
import walledin.game.network.messages.game.GetPlayerInfoResponseMessage;
import walledin.game.network.messages.game.InputMessage;
import walledin.game.network.messages.game.LoginMessage;
import walledin.game.network.messages.game.LoginResponseMessage;
import walledin.game.network.messages.game.LoginResponseMessage.ErrorCode;
import walledin.game.network.messages.game.LogoutMessage;
import walledin.game.network.messages.game.TeamSelectMessage;
import walledin.game.network.messages.masterserver.ChallengeMessage;
import walledin.game.network.messages.masterserver.GetServersMessage;
import walledin.game.network.messages.masterserver.ServerNotificationMessage;
import walledin.game.network.messages.masterserver.ServersMessage;
import walledin.game.network.server.ChangeSet;
import walledin.util.SettingsManager;

public final class Client implements NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private SocketAddress host;
    private String username;
    private final NetworkMessageWriter networkWriter;
    private final NetworkMessageReader networkReader;
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
    private final long loginRetryTime;
    private final long timeOutTime;
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

        networkWriter = new NetworkMessageWriter();
        networkReader = new NetworkMessageReader(this);
        internetServerList = new HashSet<ServerData>();
        lanServerList = new HashSet<ServerData>();
        playerList = new HashSet<PlayerClientInfo>();

        channel = DatagramChannel.open();
        masterServerChannel = DatagramChannel.open();

        loginRetryTime = SettingsManager.getInstance().getInteger(
                "network.loginRetryTime");

        timeOutTime = SettingsManager.getInstance().getInteger(
                "network.timeOutTime");
    }

    public void refreshServerList() {

        if (!connectedMasterServer) {
            return;
        }

        try {
            networkWriter.sendMessage(masterServerChannel,
                    new GetServersMessage());
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

    /**
     * Resets the received version.
     */
    public void resetReceivedVersion() {
        receivedVersion = 0;
    }

    /**
     * Update the current game state.
     * 
     * @param delta
     *            time since last update in seconds
     */
    public void update(final double delta) {
        processServerMessages();
        processMasterServerMessages();
        processBroadcastMessages();
    }

    private void processBroadcastMessages() {
        try {
            if (boundServerNotifyChannel) {
                SocketAddress address = networkReader
                        .readMessage(serverNotifyChannel);
                while (address != null) {
                    networkReader.processMessage(address);
                    address = networkReader.readMessage(serverNotifyChannel);
                }
            }
        } catch (final IOException e) {
            LOG.error("Generic IO exception.", e);
        }
    }

    private void processMasterServerMessages() {
        try {
            if (connectedMasterServer) {
                // Read messages.
                SocketAddress address = networkReader
                        .readMessage(masterServerChannel);
                while (address != null) {
                    networkReader.processMessage(address);
                    address = networkReader.readMessage(masterServerChannel);
                }
            }

        } catch (final PortUnreachableException e) {
            clientLogicManager
                    .displayError("Could not connect to master server.");
            connectedMasterServer = false;
            LOG.fatal("The port is unreachable.");
        } catch (final IOException e) {
            clientLogicManager
                    .displayError("Could not connect to master server.");
            connectedMasterServer = false;
            LOG.error("Generic IO exception.", e);
        }
    }

    private void processServerMessages() {
        try {
            if (connected) {

                if (lastLoginTry >= 0
                        && System.currentTimeMillis() - lastLoginTry > loginRetryTime) {
                    lastLoginTry = System.currentTimeMillis();
                    networkWriter.sendMessage(channel, new LoginMessage(
                            username));
                }
                // Read messages.
                SocketAddress address = networkReader.readMessage(channel);

                /* If the address is null, no message is received. */
                if (address == null) {
                    if (System.currentTimeMillis() - lastUpdate > timeOutTime) {
                        clientLogicManager
                                .displayErrorAndDisconnect("The connection timed out.");
                    }

                } else {
                    lastUpdate = System.currentTimeMillis();
                }

                while (address != null) {
                    networkReader.processMessage(address);
                    address = networkReader.readMessage(channel);
                }
            }
        } catch (final PortUnreachableException e) {
            clientLogicManager
                    .displayErrorAndDisconnect("Connection to server lost.");
            LOG.fatal("The port is unreachable.");
        } catch (final IOException e) {
            clientLogicManager
                    .displayErrorAndDisconnect("Connection to server lost.");
            LOG.error("Generic IO exception.", e);
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
        if (!boundServerNotifyChannel) {
            try {
                serverNotifyChannel = DatagramChannel.open();
                serverNotifyChannel.socket().bind(
                        new InetSocketAddress(
                                NetworkConstants.MASTER_PROTOCOL_PORT));
                serverNotifyChannel.configureBlocking(false);
                boundServerNotifyChannel = true;
            } catch (final IOException e) {
                LOG.warn("IOException", e);
            }
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
    public void connectToServer(final String address) {
        connectToServer(new InetSocketAddress(address, 1234)); // FIXME: port hardcoded
    }

    /**
     * Connects to a game server. If already connected to a server, it will
     * disconnect if the server is a different one. If the server is the same,
     * it will do nothing.
     */
    public void connectToServer(final SocketAddress address) {
        if (connected
                && channel.socket().getRemoteSocketAddress() == address) {
            return;
        }

        try {
            lastLoginTry = System.currentTimeMillis();

            LOG.info("Connecting to server " + address);
            username = System.getProperty("user.name");

            /* Reset some variables. */
            clientLogicManager.resetGame();

            // always try to disconnect. Does nothing if not connected
            channel.disconnect();
            channel.configureBlocking(false);
            channel.connect(address);

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
                networkWriter.sendMessage(channel, new LogoutMessage());
                connected = false;
            } catch (final IOException e) {
                LOG.fatal("IOException during logout", e);
            }
        }
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
                networkWriter.sendMessage(channel, new GetPlayerInfoMessage());
            } catch (final IOException e) {
                LOG.error("IOException", e);
            }
        }
    }

    public void selectTeam(final Team team) {
        try {
            networkWriter.sendMessage(channel, new TeamSelectMessage());
        } catch (final IOException e) {
            LOG.error("IOException", e);
        }
    }

    public long getBytesRead() {
        return networkReader.getBytesRead();
    }

    public int getMessagesRead() {
        return networkReader.getMessagesRead();
    }

    public long getBytesWritten() {
        return networkWriter.getBytesWritten();
    }

    public int getMessagesWritten() {
        return networkWriter.getMessagesWritten();
    }

    public void resetStatistics() {
        networkReader.resetStatistics();
        networkWriter.resetStatistics();
    }

    /**
     * Called when the gamestate has been updated. We only send a new input when
     * we receive the net game state.
     */
    @Override
    public void receivedMessage(final SocketAddress address,
            final GamestateMessage message) {
        lastLoginTry = -1;
        // FIXME check if this is correct .. version could be swaped
        final ChangeSet changeSet = message.getChangeSet();
        // The old version from where this change set updates
        final int oldVersion = changeSet.getFirstVersion();
        // The new version to which this change set updates
        final int newVersion = message.getNewVersion();
        if (LOG.isTraceEnabled()) {
            LOG.trace("version:" + newVersion + " receivedVersion:"
                    + receivedVersion + " oldversion: " + oldVersion);
        }
        if (receivedVersion >= oldVersion && newVersion > receivedVersion) {
            clientLogicManager.getEntityManager().applyChangeSet(changeSet,
                    receivedVersion);
            receivedVersion = newVersion;
        }
        try {
            networkWriter.sendMessage(channel, new InputMessage(
                    receivedVersion, PlayerActionManager.getInstance()
                            .getPlayerActions(), renderer.screenToWorld(Input
                            .getInstance().getMousePos())));
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
            dispose();
        }
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ServersMessage message) {
        final Set<ServerData> servers = message.getServers();
        LOG.info("Received server list. " + servers.size()
                + " servers available.");
        internetServerList = servers;
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final LoginResponseMessage message) {
        if (message.getErrorCode() == ErrorCode.ERROR_SUCCESSFULL) {
            clientLogicManager.setPlayerName(message.getEntityName());
            LOG.info("Player entity name received: " + message.getEntityName());
            return;
        }

        switch (message.getErrorCode()) {
        case ERROR_SERVER_IS_FULL:
            clientLogicManager.displayErrorAndDisconnect("The server is full.");
            break;
        default:
            clientLogicManager.displayErrorAndDisconnect("Could not login to"
                    + " the server: " + message.getErrorCode());
            break;
        }
    }

    @Override
    public void receivedMessage(SocketAddress address,
            ConsoleUpdateMessage message) {
        LOG.info("Server: " + message.getMessage());
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ServerNotificationMessage message) {
        lanServerList.add(message.getServer());
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetPlayerInfoResponseMessage message) {
        playerList.clear();
        playerList.addAll(message.getPlayers());
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final LoginMessage loginMessage) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ChallengeMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetPlayerInfoMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final TeamSelectMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final LogoutMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final InputMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetServersMessage message) {
        // ignore
    }
}
