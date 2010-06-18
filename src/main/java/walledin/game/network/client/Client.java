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
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkDataReader;
import walledin.game.network.NetworkDataWriter;
import walledin.game.network.NetworkEventListener;
import walledin.game.screens.GameScreen;
import walledin.game.screens.MainMenuScreen;
import walledin.game.screens.Screen;
import walledin.game.screens.ScreenManager;
import walledin.game.screens.Screen.ScreenState;
import walledin.game.screens.ScreenManager.ScreenType;
import walledin.util.Utils;

public class Client implements RenderListener, NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static final int PORT = 1234;

    private final Renderer renderer; // current renderer
    private final ScreenManager screenManager;
    private Screen gameScreen;
    private final SocketAddress host;
    private final String username;
    private final NetworkDataWriter networkDataWriter;
    private final NetworkDataReader networkDataReader;
    private final DatagramChannel channel;
    private boolean quitting = false;

    /** Keeps track if the player is connected to a server. */
    private boolean connected = false;
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
        quitting = false;
        lastLoginTry = System.currentTimeMillis();
        // Hardcode the host and username for now
        host = new InetSocketAddress("localhost", PORT);
        username = System.getProperty("user.name");
        channel = DatagramChannel.open();
    }

    public static void main(final String[] args) {
        final Renderer renderer = new Renderer();
        Client client;
        try {
            client = new Client(renderer);
        } catch (final IOException e) {
            LOG.fatal("IO exception while creating client.", e);
            return;
        }
        LOG.info("initializing renderer");
        renderer.initialize("WalledIn", 800, 600, false);
        renderer.addListener(client);
        // Start renderer
        LOG.info("starting renderer");
        renderer.beginLoop();
    }

    /**
     * Called when the gamestate has been updated. We only send a new input when
     * we receive the net game state
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
            networkDataWriter.sendInputMessage(channel, receivedVersion, Input
                    .getInstance().getKeysDown(), renderer.screenToWorld(Input
                    .getInstance().getMousePos()), Input.getInstance()
                    .getMouseDown());
        } catch (final IOException e) {
            LOG.error("IO exception during network event", e);
            dispose();
        }
        return result;
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
            final int newVersion, final Set<Integer> keys,
            final Vector2f mousePos, final Boolean mouseDown) {
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

        if (connected) {
            try {

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
            } catch (final PortUnreachableException e) {
                LOG
                        .fatal("Could not connect to server. PortUnreachableException");
                dispose();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
     * Initialize game
     */
    @Override
    public void init() {
        LOG.info("initializing client");

        /* Load standard font */
        final Font font = new Font();
        font.readFromStream(Utils.getClasspathURL("arial20.font"));
        screenManager.addFont("arial20", font);

        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen(null);
        // gameScreen.setState(ScreenState.Visible);
        screenManager.addScreen(ScreenType.GAME, gameScreen);
        final Screen menuScreen = new MainMenuScreen(null);
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.initialize();
        menuScreen.setState(ScreenState.Visible);
        renderer.hideHardwareCursor();

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
    }

    /**
     * Connects to a game server.
     */
    public final void connectToServer() {
        LOG.info("configure network channel and connecting to server");
        try {
            channel.configureBlocking(false);
            channel.connect(host);

            final String playerEntityName = NetworkConstants
                    .getAddressRepresentation(channel.socket()
                            .getLocalSocketAddress());
            screenManager.setPlayerName(playerEntityName);
            
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

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();
            try {
                networkDataWriter.sendLogoutMessage(channel);
            } catch (final IOException e) {
                LOG.fatal("IOException during logout", e);
            }
        }
    }
}
