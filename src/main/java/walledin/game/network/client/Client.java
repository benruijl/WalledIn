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
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;
import walledin.game.PlayerActionManager;
import walledin.game.PlayerActions;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkDataReader;
import walledin.game.network.NetworkDataWriter;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.ServerData;
import walledin.game.screens.GameScreen;
import walledin.game.screens.MainMenuScreen;
import walledin.game.screens.Screen;
import walledin.game.screens.ScreenManager;
import walledin.game.screens.ScreenManager.ScreenType;
import walledin.game.screens.ServerListScreen;
import walledin.util.SettingsManager;
import walledin.util.Utils;

public class Client implements RenderListener, NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static final int PORT = 1234;

    private final Renderer renderer; // current renderer
    private final ScreenManager screenManager;
    private Screen gameScreen;
    private SocketAddress host;
    private String username;
    private final NetworkDataWriter networkDataWriter;
    private final NetworkDataReader networkDataReader;
    private final DatagramChannel channel;
    private final DatagramChannel masterServerChannel;
    private Set<ServerData> serverList;
    private boolean quitting = false;

    /** Keeps track if the player is connected to a server. */
    private boolean connected = false;
    private boolean connectedMasterServer = false;
    private int receivedVersion = 0;
    private long lastLoginTry;
    // in milliseconds
    private final long LOGIN_RETRY_TIME = 1000;

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
        serverList = new HashSet<ServerData>();
        quitting = false;

        channel = DatagramChannel.open();
        masterServerChannel = DatagramChannel.open();
    }

    public static void main(final String[] args) {
        /* Load configuration */
        try {
            SettingsManager.getInstance().loadSettings(
                    Utils.getClasspathURL("settings.ini"));
        } catch (IOException e) {
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

        SettingsManager settings = SettingsManager.getInstance();

        renderer.initialize("WalledIn",
                settings.getInteger("engine.window.width"),
                settings.getInteger("engine.window.height"),
                settings.getBoolean("engine.window.fullScreen"));
        renderer.addListener(client);
        // Start renderer
        LOG.info("starting renderer");
        renderer.beginLoop();
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
            PlayerActionManager.getInstance().update();

            networkDataWriter.sendInputMessage(channel, receivedVersion,
                    PlayerActionManager.getInstance().getPlayerActions(),
                    renderer.screenToWorld(Input.getInstance().getMousePos()));
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
            dispose();
        }
        return result;
    }

    public void refreshServerList() {
        try {
            networkDataWriter.sendGetServersMessage(masterServerChannel);
        } catch (final IOException e) {
            LOG.error("IOException", e);
        }
    }

    public Set<ServerData> getServerList() {
        return serverList;
    }

    @Override
    public void receivedServersMessage(final SocketAddress address,
            final Set<ServerData> servers) {
        LOG.info("Received server list. " + servers.size()
                + " servers available.");
        serverList = servers;
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

    /**
     * Update the current game state
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
                    networkDataWriter.sendLoginMessage(channel, username);
                }
                // Read messages.
                boolean hasMore = networkDataReader.recieveMessage(channel,
                        screenManager.getEntityManager());
                while (hasMore) {
                    hasMore = networkDataReader.recieveMessage(channel,
                            screenManager.getEntityManager());
                }
            }
            if (connectedMasterServer) {
                // Read messages.
                boolean hasMore = networkDataReader.recieveMessage(
                        masterServerChannel, screenManager.getEntityManager());
                while (hasMore) {
                    hasMore = networkDataReader.recieveMessage(
                            masterServerChannel,
                            screenManager.getEntityManager());
                }
            }
        } catch (final PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            dispose();
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
        font.readFromStream(Utils.getClasspathURL("arial20.font"));
        screenManager.addFont("arial20", font);

        final Screen serverListScreen = new ServerListScreen();
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

        // create cursor
        final Entity cursor = screenManager.getEntityManager().create(
                Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        renderer.hideHardwareCursor();

        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen();
        screenManager.addScreen(ScreenType.GAME, gameScreen);

        // this screen has to be active, because it updates the mouse
        // and receives gamestate changes, even if the menus are opened.
        gameScreen.setActive(true);
        final Screen menuScreen = new MainMenuScreen();
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.initialize();
        menuScreen.setActiveAndVisible();

        connectToMasterServer();
    }

    /**
     * Connects to a game server.
     */
    public final void connectToServer(final ServerData server) {
        LOG.info("configure network channel and connecting to server");
        try {
            lastLoginTry = System.currentTimeMillis();

            LOG.info(server.getAddress());
            host = server.getAddress();
            username = System.getProperty("user.name");

            channel.configureBlocking(false);
            channel.connect(host);

            // the client is connected now
            connected = true;
        } catch (final PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            dispose();
        }
    }

    /**
     * Connects to a master server.
     */
    public final void connectToMasterServer() {
        LOG.info("configure network channel and connecting to master server");
        try {
            masterServerChannel.configureBlocking(false);
            masterServerChannel.connect(NetworkConstants.MASTERSERVER_ADDRESS);
            connectedMasterServer = true;
        } catch (final PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException", e);
            dispose();
        }
    }

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();

            if (connected) {
                try {
                    networkDataWriter.sendLogoutMessage(channel);
                } catch (final IOException e) {
                    LOG.fatal("IOException during logout", e);
                }
            }
        }
    }

    @Override
    public void receivedLoginReponseMessage(final SocketAddress address,
            final String playerEntityName) {
        screenManager.setPlayerName(playerEntityName);
        LOG.info("Player entity name received: " + playerEntityName);
    }

}
