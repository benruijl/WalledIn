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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.game.GameLogicManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.ServerData;
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
import walledin.util.SettingsManager;
import walledin.util.Utils;

/**
 * This class provides the server for the game. All gamestate updates happen
 * here. Clients can register to this class to be added to the game.
 * 
 */
public class Server implements NetworkEventListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Server.class);

    private final long broadcastInterval;

    /** Server port. */
    private final int port;

    /** Updates per second. */
    private final int updatesPerSecond;

    /** The maximum number of frames stored in memory. */
    private final int storedChangesets;

    private final String serverName;
    private final long challengeTimeout;
    private final Map<SocketAddress, PlayerConnection> players;
    private final int maxPlayers;
    private boolean running;
    private final NetworkMessageWriter networkWriter;
    private final NetworkMessageReader networkReader;
    private long currentTime;
    private final GameLogicManager gameLogicManager;
    private final Queue<ChangeSet> changeSets;
    private final Map<Integer, ChangeSet> changeSetLookup;

    private DatagramChannel masterServerChannel;
    private DatagramChannel channel;
    private DatagramChannel serverNotifySocket;
    private long lastChallenge;
    private long lastBroadcast;

    /**
     * Creates a new server. Initializes variables to their default values.
     */
    public Server(final GameLogicManager gameLogicManager) {
        players = new HashMap<SocketAddress, PlayerConnection>();
        running = false;
        networkWriter = new NetworkMessageWriter();
        networkReader = new NetworkMessageReader(this);
        changeSetLookup = new HashMap<Integer, ChangeSet>();
        changeSets = new LinkedList<ChangeSet>();
        this.gameLogicManager = gameLogicManager;

        /* Load settings */
        updatesPerSecond = SettingsManager.getInstance().getInteger(
                "general.updatesPerSecond");
        storedChangesets = SettingsManager.getInstance().getInteger(
                "general.storageCapacity");
        port = SettingsManager.getInstance().getInteger("network.port");
        challengeTimeout = SettingsManager.getInstance().getInteger(
                "network.challengeTimeOut");
        broadcastInterval = SettingsManager.getInstance().getInteger(
                "network.lanBroadcastInterval");
        serverName = SettingsManager.getInstance().getString("game.serverName");
        maxPlayers = SettingsManager.getInstance()
                .getInteger("game.maxPlayers");

        // Store the first version so we can give it new players
        final ChangeSet firstChangeSet = gameLogicManager.getEntityManager()
                .createChangeSet();
        changeSetLookup.put(firstChangeSet.getFirstVersion(), firstChangeSet);
    }

    /**
     * Runs the server. It starts a server channel and enters the main loop.
     * 
     * @throws IOException
     */
    public final void run() throws IOException {
        LOG.info("initializing");
        init();
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        serverNotifySocket = DatagramChannel.open();
        serverNotifySocket.socket().setBroadcast(true);
        serverNotifySocket.configureBlocking(false);
        masterServerChannel = DatagramChannel.open();
        masterServerChannel.connect(NetworkConstants.MASTERSERVER_ADDRESS);
        masterServerChannel.configureBlocking(false);

        lastChallenge = System.currentTimeMillis();
        lastBroadcast = System.currentTimeMillis();

        networkWriter.sendMessage(masterServerChannel,
                new ServerNotificationMessage(createServerData()));

        currentTime = System.nanoTime(); // initialize
        running = true;
        LOG.info("starting main loop");
        while (running) {
            final long time = System.nanoTime();
            doLoop();
            double delta = System.nanoTime() - time;
            // convert to sec
            delta /= 1000000000;
            // Calculate the how many milliseconds are left
            final long left = (long) ((1d / updatesPerSecond - delta) * 1000);
            try {
                if (left > 0) {
                    Thread.sleep(left);
                }
            } catch (final InterruptedException e) {
                LOG.fatal("Interrupted in network loop!", e);
                return;
            }
        }
    }

    /**
     * Main loop of the server. Takes care of reading messages, updating
     * gamestate, and sending messages.
     * 
     * @throws IOException
     */
    private void doLoop() throws IOException {
        // Read input messages and login messages
        SocketAddress address = networkReader.readMessage(channel);
        while (address != null) {
            networkReader.processMessage(address);
            address = networkReader.readMessage(channel);
        }

        if (lastChallenge < System.currentTimeMillis() - challengeTimeout) {
            LOG.warn("Did not recieve challenge from master server yet! "
                    + "Sending new notification.");
            lastChallenge = System.currentTimeMillis();
            try {
                networkWriter.sendMessage(masterServerChannel,
                        new ServerNotificationMessage(createServerData()));
            } catch (final PortUnreachableException e) {
                LOG.warn("The port of the master server is unreachable. "
                        + "This means that either the master server is down, "
                        + "or you haven't opened the correct ports.");
            } catch (final IOException e) {
                LOG.error("IOException in communication with master server", e);
            }
        }

        if (lastBroadcast < System.currentTimeMillis() - broadcastInterval) {
            networkWriter.sendMessage(serverNotifySocket,
                    NetworkConstants.BROADCAST_ADDRESS,
                    new ServerNotificationMessage(createServerData()));
            lastBroadcast = System.currentTimeMillis();
        }

        double delta = System.nanoTime() - currentTime;
        currentTime = System.nanoTime();
        // convert to sec
        delta /= 1000000000.0;

        // Update game state
        gameLogicManager.update(delta);
        // Process the changes
        processChanges();
        // Write to all the clients
        sendGamestate(channel);
    }

    private ServerData createServerData() {
        final InetSocketAddress address = new InetSocketAddress(port);
        final ServerData data = new ServerData(address, serverName,
                players.size(), maxPlayers, gameLogicManager.getGameMode());
        return data;
    }

    private void processChanges() {
        // Get the oldest changeset (we dont support this version anymore from
        // now)
        final ChangeSet oldChangeSet = changeSets.remove();
        changeSetLookup.remove(oldChangeSet.getFirstVersion());
        // Remove the player that are still on this version or older
        final Set<SocketAddress> removedPlayers = new HashSet<SocketAddress>();
        for (final PlayerConnection connection : players.values()) {
            if (connection.getReceivedVersion() <= oldChangeSet.getFirstVersion()) {
                removedPlayers.add(connection.getAddress());
                LOG.info("Connection lost to client " + connection.getAddress());
            }
        }
        for (final SocketAddress address : removedPlayers) {
            removePlayer(address);
        }
        // Get current change set from entity manager and merge it with all the
        // save versions
        final ChangeSet currentChangeSet = gameLogicManager.getEntityManager()
                .createChangeSet();
        for (final ChangeSet changeSet : changeSetLookup.values()) {
            changeSet.merge(currentChangeSet);
        }
        // Add the current change set
        changeSets.add(currentChangeSet);
        changeSetLookup.put(currentChangeSet.getFirstVersion(), currentChangeSet);
    }

    /**
     * Writes updated game information to both new and current players. The new
     * players receive extra data.
     * 
     * @param channel
     *            The channel to send to
     * @throws IOException
     */
    private void sendGamestate(final DatagramChannel channel)
            throws IOException {
        final int currentVersion = gameLogicManager.getEntityManager()
                .getCurrentVersion();
        for (final PlayerConnection connection : players.values()) {
            // Get the version that the client has already received
            int connectionRecievedVersion = connection.getReceivedVersion();
            if (connection.isNew()) {
                // Set to first version
                connectionRecievedVersion = 0;
            }

            final ChangeSet changeSet = changeSetLookup
                    .get(connectionRecievedVersion);

            if (changeSet == null) {
                LOG.error("Could not find changeset with version "
                        + connectionRecievedVersion);
                continue;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("currentVersion: " + currentVersion + " changeset: "
                        + changeSet.getFirstVersion() + " " + changeSet.getCreated()
                        + " " + changeSet.getRemoved() + " "
                        + changeSet.getUpdated());
            }
            networkWriter.sendMessage(channel, connection.getAddress(),
                    new GamestateMessage(changeSet, currentVersion));
        }
    }

    /**
     * Removes the player and player specific entities, like their cursor.
     * 
     * @param address
     */
    private void removePlayer(final SocketAddress address) {
        final PlayerConnection connection = players.remove(address);
        gameLogicManager.removePlayer(connection.getPlayer().getName());
    }

    /**
     * Initializes the game. It reads the default map and initializes the entity
     * manager.
     */
    public final void init() {
        // Fill the change set queue
        for (int i = 0; i < storedChangesets; i++) {
            final ChangeSet changeSet = gameLogicManager.getEntityManager()
                    .createChangeSet();
            for (final ChangeSet oldChangeSet : changeSetLookup.values()) {
                oldChangeSet.merge(changeSet);
            }
            changeSets.add(changeSet);
            changeSetLookup.put(changeSet.getFirstVersion(), changeSet);
        }

        gameLogicManager.initialize();
    }

    /**
     * Creates a connection to a new client.
     * 
     * @param address
     *            Player socket address
     */
    @Override
    public void receivedMessage(final SocketAddress address,
            final LoginMessage message) {
        final String entityName = Utils.getAddressRepresentation(address);
        ErrorCode error = ErrorCode.ERROR_LOGIN_FAILED;

        // Check if this player is already logged in
        if (!players.containsKey(address) && players.size() < maxPlayers) {
            final Entity player = gameLogicManager.createPlayer(entityName,
                    message.getName());

            final PlayerConnection con = new PlayerConnection(address, player,
                    gameLogicManager.getEntityManager().getCurrentVersion());
            players.put(address, con);

            LOG.info("new player " + message.getName() + " @ " + address);
            error = ErrorCode.ERROR_SUCCESSFULL;

        }

        if (players.size() >= maxPlayers) {
            error = ErrorCode.ERROR_SERVER_IS_FULL;
        }

        // send the client the unique entity name of the player
        try {
            networkWriter.sendMessage(channel, address,
                    new LoginResponseMessage(error, entityName));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ChallengeMessage message) {
        try {
            lastChallenge = System.currentTimeMillis();
            networkWriter.sendMessage(channel, address, message);
        } catch (final IOException e) {
            LOG.error("IOException during challengeResponse", e);
        }
    }

    /**
     * Log the player out.
     */
    @Override
    public void receivedMessage(final SocketAddress address,
            final LogoutMessage message) {
        LOG.info("Player " + address.toString() + " left the game.");
        removePlayer(address);
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final InputMessage message) {
        final PlayerConnection connection = players.get(address);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Input message: " + message.getVersion() + " recieved: "
                    + connection.getReceivedVersion());
        }
        if (connection != null
                && message.getVersion() > connection.getReceivedVersion()) {
            connection.setNew();
            connection.setPlayerActions(message.getPlayerActions());
            connection.setMousePos(message.getMousePos());

            // also send the received data to the player
            connection.getPlayer().setAttribute(Attribute.PLAYER_ACTIONS,
                    message.getPlayerActions());

            connection.getPlayer().setAttribute(Attribute.CURSOR_POS,
                    message.getMousePos());
            connection.setReceivedVersion(message.getVersion());
        }
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetPlayerInfoMessage message) {
        try {
            networkWriter.sendMessage(channel, address,
                    new GetPlayerInfoResponseMessage(gameLogicManager
                            .getPlayers().values()));
        } catch (final IOException e) {
            LOG.error("IOException during GetPlayerInfo", e);
        }
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final TeamSelectMessage message) {
        final PlayerConnection connection = players.get(address);

        /*
         * Sometimes the login process takes longer than for this message to
         * arrive. Then the connection is not made yet, so we check it.
         */
        if (connection != null) {
            final String entityName = connection.getPlayer().getName();
            gameLogicManager.setTeam(entityName, message.getTeam());
        }

    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GamestateMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ServersMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final ServerNotificationMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final LoginResponseMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetServersMessage message) {
        // ignore
    }

    @Override
    public void receivedMessage(final SocketAddress address,
            final GetPlayerInfoResponseMessage message) {
        // ignore
    }
}
