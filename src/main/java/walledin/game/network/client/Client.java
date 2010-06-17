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
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
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
import walledin.game.screens.ScreenType;
import walledin.game.screens.Screen.ScreenState;
import walledin.util.Utils;

public class Client implements RenderListener, NetworkEventListener, Runnable {
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
    private String playerEntityName;
    private boolean quitting = false;
    private int receivedVersion = 0;

    /**
     * Create the client
     * 
     * @param renderer
     *            Current renderer
     * @throws IOException
     */
    public Client(final Renderer renderer) throws IOException {
        this.renderer = renderer;
        screenManager = new ScreenManager(this, renderer);
        
        //entityFactory = new EntityFactory();
        //entityManager = new EntityManager(entityFactory);
        networkDataWriter = new NetworkDataWriter();
        networkDataReader = new NetworkDataReader(this);
        quitting = false;
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
     * Run network code
     */
    @Override
    public void run() {
        try {
            doRun();
        } catch (final IOException e) {
            LOG.fatal("IOException during network loop", e);
        }
    }

    /**
     * Do the actual network loop.
     * 
     * @throws IOException
     */
    private void doRun() throws IOException {
        channel.configureBlocking(true);
        channel.connect(host);
        final NetworkInterface networkInterface = NetworkInterface
                .getByInetAddress(channel.socket().getLocalAddress());
        LOG.debug("Connection MTU: " + networkInterface.getMTU());
        playerEntityName = NetworkConstants.getAddressRepresentation(channel
                .socket().getLocalSocketAddress());
        
        // Register player entity name with screen manager
        screenManager.setPlayerName(playerEntityName);
        
        LOG.debug(playerEntityName);
        networkDataWriter.sendLoginMessage(channel, username);
        LOG.info("starting network loop");
        while (!quitting) {
            // Read messages. Locks on the entity manager to prevent renderer or
            // update from being performed half way
            networkDataReader.recieveMessage(channel, screenManager.getEntityManager());
        }
        // write logout message
        networkDataWriter.sendLogoutMessage(channel);
    }

    /**
     * Called when the gamestate has been updated. We only send a new input when
     * we receive the net game state
     */
    @Override
    public boolean receivedGamestateMessage(final SocketAddress address,
            final int oldVersion, final int newVersion) {
        boolean result = false;
        if (LOG.isTraceEnabled()) {
            LOG.trace("version:" + newVersion + " receivedVersion:"
                    + receivedVersion);
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
        Font font = new Font();
        font.readFromStream(Utils.getClasspathURL("arial20.font"));
        screenManager.addFont("arial20", font);
        
        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen();
        //gameScreen.setState(ScreenState.Visible);
        screenManager.addScreen(ScreenType.GAME, gameScreen);
        gameScreen.initialize(); // load textures, etc.
        Screen menuScreen = new MainMenuScreen();
        screenManager.addScreen(ScreenType.MAIN_MENU, menuScreen);
        menuScreen.setState(ScreenState.Visible);
        

        try {
            screenManager.getEntityFactory().loadScript(Utils
                    .getClasspathURL("entities/entities.groovy"));
            screenManager.getEntityFactory().loadScript(Utils
                    .getClasspathURL("entities/cliententities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
        }
        // initialize entity manager
        screenManager.getEntityManager().init();

        // create cursor
        Entity cursor = screenManager.getEntityManager().create(Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        

        LOG.info("starting network thread");
        // start network thread
        final Thread thread = new Thread(this, "network");
        thread.start();
    }

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();
        }
    }
}
