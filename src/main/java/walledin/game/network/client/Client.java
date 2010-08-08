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
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.audio.Audio;
import walledin.engine.math.Vector2f;
import walledin.game.GameLogicManager;
import walledin.game.GameLogicManager.PlayerClientInfo;
import walledin.game.PlayerActionManager;
import walledin.game.PlayerActions;
import walledin.game.Teams;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.gui.GameScreen;
import walledin.game.gui.MainMenuScreen;
import walledin.game.gui.Screen;
import walledin.game.gui.ScreenManager;
import walledin.game.gui.ScreenManager.ScreenType;
import walledin.game.gui.SelectTeamScreen;
import walledin.game.gui.ServerListScreen;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkConstants.ErrorCodes;
import walledin.game.network.NetworkDataReader;
import walledin.game.network.NetworkDataWriter;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;
import walledin.util.SettingsManager;
import walledin.util.Utils;

public final class Client implements RenderListener, NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private final Renderer renderer; // current renderer
    private final ScreenManager screenManager;
    private Screen gameScreen;
    private SocketAddress host;
    private String username;
    private final NetworkDataWriter networkDataWriter;
    private final NetworkDataReader networkDataReader;
    private final DatagramChannel channel;
    private final DatagramChannel masterServerChannel;
    private DatagramChannel serverNotifyChannel;
    private boolean quitting = false;

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

    /**
     * Create the client.
     * 
     * @param renderer
     *            Current renderer
     * @throws IOException
     */
    public Client(final Renderer renderer) throws IOException {
        this.renderer = renderer;
        screenManager = new ScreenManager(this, renderer);

        networkDataWriter = new NetworkDataWriter();
        networkDataReader = new NetworkDataReader(this);
        internetServerList = new HashSet<ServerData>();
        lanServerList = new HashSet<ServerData>();
        playerList = new HashSet<GameLogicManager.PlayerClientInfo>();
        quitting = false;

        channel = DatagramChannel.open();
        masterServerChannel = DatagramChannel.open();

        LOGIN_RETRY_TIME = SettingsManager.getInstance().getInteger(
                "network.loginRetryTime");

        TIME_OUT_TIME = SettingsManager.getInstance().getInteger(
                "network.timeOutTime");

        /* Load music */
        Audio.getInstance().loadOggSample("background1",
                Utils.getClasspathURL("audio/Clausterphobia.ogg"));

        for (int i = 1; i < 5; i++) {
            Audio.getInstance().loadWaveSample("handgun" + i,
                    Utils.getClasspathURL("audio/handgun_" + i + ".wav"));
            Audio.getInstance().loadWaveSample("foamgun" + i,
                    Utils.getClasspathURL("audio/foamgun_" + i + ".wav"));
        }

        /* Play background music */
        Audio.getInstance().playSample("background1", new Vector2f(), true);
    }

    public static void main(final String[] args) {
        /* Load configuration */
        try {
            SettingsManager.getInstance().loadSettings(
                    Utils.getClasspathURL("settings.ini"));
        } catch (final IOException e) {
            LOG.error("Could not read configuration file.", e);
        }

        final Renderer renderer = new Renderer();

        Client client;
        try {
            client = new Client(renderer);
        } catch (final IOException e) {
            LOG.fatal("IO exception while creating client.", e);
            return;
        }
        LOG.info("initializing renderer");

        final SettingsManager settings = SettingsManager.getInstance();

        renderer.initialize("WalledIn",
                settings.getInteger("engine.window.width"),
                settings.getInteger("engine.window.height"),
                settings.getBoolean("engine.window.fullScreen"));
        renderer.addListener(client);
        // Start renderer
        LOG.info("starting renderer");
        renderer.beginLoop();
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
    public void entityCreated(Entity entity) {
        // TODO: move to a better place
        /* Play a sound when a bullet is created */
        Random generator = new Random();
        int num = generator.nextInt(4) + 1;

        if (entity.getFamily() == Family.HANDGUN_BULLET) {
            Audio.getInstance().playSample("handgun" + num, new Vector2f(), false);
        }

        if (entity.getFamily() == Family.FOAMGUN_BULLET) {
            Audio.getInstance().playSample("foamgun" + num, new Vector2f(), false);
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
            // update the player actions
            // TODO: do somewhere else?

            /* Only register actions when the game screen has the focus. */
            if (screenManager.getFocusedScreen() != screenManager
                    .getScreen(ScreenType.GAME)) {
                PlayerActionManager.getInstance().clear();
            } else {
                PlayerActionManager.getInstance().update();
            }

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

    /**
     * Displays an error message, disconnects from the server and returns to the
     * server list.
     * 
     * @param message
     *            Message to display
     */
    public void displayErrorAndDisconnect(final String message) {
        screenManager.createDialog(message);
        connected = false;
        screenManager.getScreen(ScreenType.SERVER_LIST).show();
        screenManager.getScreen(ScreenType.GAME).hide();
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
            final int newVersion, final Set<PlayerActions> playerActions,
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
            screenManager.setPlayerName(playerEntityName);
            LOG.info("Player entity name received: " + playerEntityName);
            return;
        }

        switch (errorCode) {
        case ERROR_SERVER_IS_FULL:
            displayErrorAndDisconnect("The server is full.");
            break;
        default:
            displayErrorAndDisconnect("Could not login to the server.");
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
    @Override
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
                        connected = false;
                        LOG.fatal("Connection timed out.");
                        screenManager.createDialog("The connection timed out.");
                        screenManager.getScreen(ScreenType.SERVER_LIST).show();
                        screenManager.getScreen(ScreenType.GAME).hide();
                    }

                } else {
                    lastUpdate = System.currentTimeMillis();
                }

                while (address != null) {
                    networkDataReader.processMessage(address,
                            screenManager.getEntityManager());
                    address = networkDataReader.readMessage(channel);
                }
            }
            if (connectedMasterServer) {
                // Read messages.
                SocketAddress address = networkDataReader
                        .readMessage(masterServerChannel);
                while (address != null) {
                    networkDataReader.processMessage(address,
                            screenManager.getEntityManager());
                    address = networkDataReader
                            .readMessage(masterServerChannel);
                }
            }
            if (boundServerNotifyChannel) {
                SocketAddress address = networkDataReader
                        .readMessage(serverNotifyChannel);
                while (address != null) {
                    networkDataReader.processMessage(address,
                            screenManager.getEntityManager());
                    address = networkDataReader
                            .readMessage(serverNotifyChannel);
                }
            }
        } catch (final PortUnreachableException e) {
            screenManager.createDialog("Connection to server lost.");
            LOG.fatal("Connection to server lost. The port is unreachable.");
            connected = false;
        } catch (final IOException e) {
            screenManager.createDialog("Connection to server lost.");
            LOG.fatal("IOException", e);
            connected = false;
        }

        screenManager.update(delta);
    }

    /**
     * Render the current gamestate.
     */
    @Override
    public void draw(final Renderer renderer) {
        screenManager.draw(renderer);
    }

    /**
     * Initialize game.
     */
    @Override
    public void init() {
        LOG.info("initializing client");

        /* Load standard font */
        final Font font = new Font();
        final String fontName = SettingsManager.getInstance().getString(
                "game.font");
        font.readFromStream(Utils.getClasspathURL(fontName + ".font"));
        screenManager.addFont(fontName, font);

        final Screen serverListScreen = new ServerListScreen(screenManager);
        screenManager.addScreen(ScreenType.SERVER_LIST, serverListScreen);

        try {
            screenManager.getEntityFactory().loadScript(
                    Utils.getClasspathURL("entities/entities.groovy"));
            screenManager.getEntityFactory().loadScript(
                    Utils.getClasspathURL("entities/cliententities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
            dispose();
        }
        // initialize entity manager
        screenManager.getEntityManager().init();

        // create cursor, use the factory so it does not get added to the list
        final Entity cursor = screenManager.getEntityFactory().create(
                screenManager.getEntityManager(), Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        renderer.hideHardwareCursor();

        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen(screenManager);
        screenManager.addScreen(ScreenType.GAME, gameScreen);
        final Screen selectTeamScreen = new SelectTeamScreen(screenManager);
        screenManager.addScreen(ScreenType.SELECT_TEAM, selectTeamScreen);
        final Screen menuScreen = new MainMenuScreen(screenManager);
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.initialize();
        menuScreen.show();

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
            screenManager.createDialog("Could not connect to server.");
        }
    }

    public void disconnectFromServer() throws IOException {
        connected = false;
        channel.disconnect();
    }

    public boolean connectedToServer() {
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
            screenManager.createDialog("Could not connect to master server.");
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            screenManager.createDialog("Could not connect to master server.");
        }
    }

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();

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
            final Teams team) {
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

    public void selectTeam(final Teams team) {
        networkDataWriter.prepareTeamSelectMessage(team);
        try {
            networkDataWriter.sendBuffer(channel);
        } catch (final IOException e) {
            LOG.error("IOException", e);
        }
    }

}
