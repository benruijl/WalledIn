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

import walledin.engine.math.Vector2f;
import walledin.game.GameLogicManager;
import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.PlayerAction;
import walledin.game.Team;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.MessageType;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkConstants.ErrorCodes;
import walledin.game.network.NetworkDataReader;
import walledin.game.network.NetworkDataWriter;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;
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

    private final long BROADCAST_INTERVAL;

    /** Server port. */
    private final int PORT;

    /** Updates per second. */
    private final int UPDATES_PER_SECOND;

    /** The maximum number of frames stored in memory. */
    private final int STORED_CHANGESETS;

    private final String SERVER_NAME;
    private final long CHALLENGE_TIMEOUT;
    private final Map<SocketAddress, PlayerConnection> players;
    private final int maxPlayers;
    private boolean running;
    private final NetworkDataWriter networkWriter;
    private final NetworkDataReader networkReader;
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
        networkWriter = new NetworkDataWriter();
        networkReader = new NetworkDataReader(this);
        changeSetLookup = new HashMap<Integer, ChangeSet>();
        changeSets = new LinkedList<ChangeSet>();
        this.gameLogicManager = gameLogicManager;

        /* Load settings */
        UPDATES_PER_SECOND = SettingsManager.getInstance().getInteger(
                "general.updatesPerSecond");
        STORED_CHANGESETS = SettingsManager.getInstance().getInteger(
                "general.storageCapacity");
        PORT = SettingsManager.getInstance().getInteger("network.port");
        CHALLENGE_TIMEOUT = SettingsManager.getInstance().getInteger(
                "network.challengeTimeOut");
        BROADCAST_INTERVAL = SettingsManager.getInstance().getInteger(
                "network.lanBroadcastInterval");
        SERVER_NAME = SettingsManager.getInstance()
                .getString("game.serverName");
        maxPlayers = SettingsManager.getInstance()
                .getInteger("game.maxPlayers");

        // Store the first version so we can give it new players
        final ChangeSet firstChangeSet = gameLogicManager.getEntityManager()
                .getChangeSet();
        changeSetLookup.put(firstChangeSet.getVersion(), firstChangeSet);
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
        channel.socket().bind(new InetSocketAddress(PORT));
        channel.configureBlocking(false);
        serverNotifySocket = DatagramChannel.open();
        serverNotifySocket.socket().setBroadcast(true);
        serverNotifySocket.configureBlocking(false);
        masterServerChannel = DatagramChannel.open();
        masterServerChannel.connect(NetworkConstants.MASTERSERVER_ADDRESS);
        masterServerChannel.configureBlocking(false);

        lastChallenge = System.currentTimeMillis();
        lastBroadcast = System.currentTimeMillis();

        networkWriter.prepareServerNotificationResponse(PORT, SERVER_NAME,
                players.size(), maxPlayers, gameLogicManager.getGameMode());
        networkWriter.sendBuffer(masterServerChannel);

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
            final long left = (long) ((1d / UPDATES_PER_SECOND - delta) * 1000);
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
            networkReader.processMessage(address,
                    gameLogicManager.getEntityManager());
            address = networkReader.readMessage(channel);
        }

        if (lastChallenge < System.currentTimeMillis() - CHALLENGE_TIMEOUT) {
            LOG.warn("Did not recieve challenge from master server yet! "
                    + "Sending new notification.");
            lastChallenge = System.currentTimeMillis();
            try {
                networkWriter.prepareServerNotificationResponse(PORT,
                        SERVER_NAME, players.size(), maxPlayers,
                        gameLogicManager.getGameMode());
                networkWriter.sendBuffer(masterServerChannel);
            } catch (PortUnreachableException e) {
                LOG.warn("The port of the master server is unreachable. "
                        + "This means that either the master server is down, "
                        + "or you haven't opened the correct ports.");
            } catch (IOException e) {
                LOG.error("IOException in communication with master server", e);
            }
        }

        if (lastBroadcast < System.currentTimeMillis() - BROADCAST_INTERVAL) {
            networkWriter.prepareServerNotificationResponse(PORT, SERVER_NAME,
                    players.size(), maxPlayers, gameLogicManager.getGameMode());
            networkWriter.sendBuffer(serverNotifySocket,
                    NetworkConstants.BROADCAST_ADDRESS);
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

    private void processChanges() {
        // Get the oldest changeset (we dont support this version anymore from
        // now)
        final ChangeSet oldChangeSet = changeSets.remove();
        changeSetLookup.remove(oldChangeSet.getVersion());
        // Remove the player that are still on this version or older
        final Set<SocketAddress> removedPlayers = new HashSet<SocketAddress>();
        for (final PlayerConnection connection : players.values()) {
            if (connection.getReceivedVersion() <= oldChangeSet.getVersion()) {
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
                .getChangeSet();
        for (final ChangeSet changeSet : changeSetLookup.values()) {
            changeSet.merge(currentChangeSet);
        }
        // Add the current change set
        changeSets.add(currentChangeSet);
        changeSetLookup.put(currentChangeSet.getVersion(), currentChangeSet);
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
            int sendVersion = connection.getReceivedVersion();
            if (connection.isNew()) {
                // Set to first version
                sendVersion = 0;
            }

            final ChangeSet changeSet = changeSetLookup.get(sendVersion);

            if (LOG.isTraceEnabled()) {
                LOG.trace("currentVersion: " + currentVersion + " changeset: "
                        + changeSet.getVersion() + " " + changeSet.getCreated()
                        + " " + changeSet.getRemoved() + " "
                        + changeSet.getUpdated());
            }
            networkWriter.prepareGamestateMessage(
                    gameLogicManager.getEntityManager(), changeSet,
                    changeSet.getVersion(), currentVersion);
            networkWriter.sendBuffer(channel, connection.getAddress());
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

    @Override
    public final boolean receivedGamestateMessage(final SocketAddress address,
            final int oldVersion, final int newVersion) {
        // ignore .. should not happen
        return false;
    }

    /**
     * Creates a connection to a new client.
     * 
     * @param name
     *            Player name
     * @param address
     *            Player socket address
     */
    @Override
    public final void receivedLoginMessage(final SocketAddress address,
            final String name) {

        final String entityName = NetworkConstants
                .getAddressRepresentation(address);
        ErrorCodes error = ErrorCodes.ERROR_LOGIN_FAILED;

        // Check if this player is already logged in
        if (!players.containsKey(address) && players.size() < maxPlayers) {
            final Entity player = gameLogicManager.createPlayer(entityName,
                    name);

            final PlayerConnection con = new PlayerConnection(address, player,
                    gameLogicManager.getEntityManager().getCurrentVersion());
            players.put(address, con);

            LOG.info("new player " + name + " @ " + address);
            error = ErrorCodes.ERROR_SUCCESSFULL;

        }

        if (players.size() >= maxPlayers) {
            error = ErrorCodes.ERROR_SERVER_IS_FULL;
        }

        // send the client the unique entity name of the player
        try {
            networkWriter.prepareLoginResponseMessage(error, entityName);
            networkWriter.sendBuffer(channel, address);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void receivedChallengeMessage(final SocketAddress address,
            final long challengeData) {
        try {
            lastChallenge = System.currentTimeMillis();
            networkWriter.prepareChallengeResponse(challengeData);
            networkWriter.sendBuffer(channel, address);
        } catch (final IOException e) {
            LOG.error("IOException during challengeResponse", e);
        }
    }

    @Override
    public void receivedServersMessage(final SocketAddress address,
            final Set<ServerData> servers) {
        // ignore .. should not happen
    }

    /**
     * Log the player out.
     */
    @Override
    public final void receivedLogoutMessage(final SocketAddress address) {
        LOG.info("Player " + address.toString() + " left the game.");
        removePlayer(address);
    }

    @Override
    public final void receivedInputMessage(final SocketAddress address,
            final int newVersion, final Set<PlayerAction> playerActions,
            final Vector2f cursorPos) {
        final PlayerConnection connection = players.get(address);
        if (connection != null && newVersion > connection.getReceivedVersion()) {
            connection.setNew();
            connection.setPlayerActions(playerActions);
            connection.setMousePos(cursorPos);

            // also send the received data to the player
            connection.getPlayer().setAttribute(Attribute.PLAYER_ACTIONS,
                    playerActions);

            connection.getPlayer()
                    .setAttribute(Attribute.CURSOR_POS, cursorPos);
            connection.setReceivedVersion(newVersion);
        }
    }

    @Override
    public void receivedLoginReponseMessage(final SocketAddress address,
            final ErrorCodes errorCode, final String playerEntityName) {
        // ignore .. should not happen
    }

    @Override
    public void receivedServerNotificationMessage(final SocketAddress address,
            final ServerData server) {
        // ignore
    }

    @Override
    public void receivedGetPlayerInfoMessage(final SocketAddress address) {
        try {
            networkWriter.prepareGetPlayerInfoReponseMessage(gameLogicManager
                    .getPlayers().values());
            networkWriter.sendBuffer(channel, address);
        } catch (final IOException e) {
            LOG.error("IOException during GetPlayerInfo", e);
        }

    }

    @Override
    public void receivedGetPlayerInfoResponseMessage(
            final SocketAddress address, final Set<PlayerClientInfo> players) {
        // ignore
    }

    @Override
    public void receivedTeamSelectMessage(final SocketAddress address,
            final Team team) {
        final PlayerConnection connection = players.get(address);

        /*
         * Sometimes the login process takes longer than for this message to
         * arrive. Then the connection is not made yet, so we check it.
         */
        if (connection != null) {
            final String entityName = connection.getPlayer().getName();
            gameLogicManager.setTeam(entityName, team);
        }

    }

    /**
     * Initializes the game. It reads the default map and initializes the entity
     * manager.
     */
    public final void init() {
        // Fill the change set queue
        for (int i = 0; i < STORED_CHANGESETS; i++) {
            final ChangeSet changeSet = gameLogicManager.getEntityManager()
                    .getChangeSet();
            changeSets.add(changeSet);
            changeSetLookup.put(changeSet.getVersion(), changeSet);
        }

        gameLogicManager.initialize();
    }

    @Override
    public void entityCreated(final Entity entity) {
        // ignore
    }
}
