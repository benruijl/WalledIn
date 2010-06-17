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
import walledin.game.screens.Screen;
import walledin.game.screens.ScreenManager;
import walledin.game.screens.Screen.ScreenState;
import walledin.util.Utils;

public class Client implements RenderListener, NetworkEventListener {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static final int PORT = 1234;
    private static final int TILE_SIZE = 64;
    private static final int TILES_PER_LINE = 16;

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
    private long lastLoginTry;
    // in millisec
    private long LOGIN_RETRY_TIME = 1000;

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
            LOG.fatal("IO exception while creation of client", e);
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
        try {
            if (lastLoginTry >= 0
                    && ((System.currentTimeMillis() - lastLoginTry) > LOGIN_RETRY_TIME)) {
                lastLoginTry = System.currentTimeMillis();
                networkDataWriter.sendLoginMessage(channel, username);
            }
            // Read messages.
            boolean hasMore = networkDataReader.recieveMessage(channel,
                    entityManager);
            while (hasMore) {
                hasMore = networkDataReader.recieveMessage(channel,
                        entityManager);
            }
        } catch (PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            dispose();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        loadTextures();
        createTextureParts();

        /* Load standard font */
        Font font = new Font();
        font.readFromStream(Utils.getClasspathURL("arial20.font"));
        screenManager.addFont("arial20", font);
        
        /* Create game screen and add it to the screen manager. */
        gameScreen = new GameScreen();
        gameScreen.setState(ScreenState.Visible);
        screenManager.addScreen(gameScreen);
        

        try {
            screenManager.getEntityFactory().loadScript(Utils
                    .getClasspathURL("entities/entities.groovy"));
            screenManager.getEntityFactory().loadScript(Utils
                    .getClasspathURL("entities/cliententities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
            dispose();
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
            dispose();
        }
        // initialize entity manager
        screenManager.getEntityManager().init();

        // Background is not created by server (not yet anyway)
        screenManager.getEntityManager().create(Family.BACKGROUND, "Background");

        // create cursor
        Entity cursor = screenManager.getEntityManager().create(Family.CURSOR, "cursor");
        screenManager.setCursor(cursor);
        

        LOG.info("configure network channel");
        try {
            channel.configureBlocking(false);
            channel.connect(host);
            playerEntityName = NetworkConstants
                    .getAddressRepresentation(channel.socket()
                            .getLocalSocketAddress());
        } catch (PortUnreachableException e) {
            LOG.fatal("Could not connect to server. PortUnreachableException");
            dispose();
        } catch (IOException e) {
            LOG.fatal("IOException", e);
            dispose();
        }
    }

    private void loadTextures() {
        final TextureManager manager = TextureManager.getInstance();
        manager.loadFromURL(Utils.getClasspathURL("tiles.png"), "tiles");
        manager.loadFromURL(Utils.getClasspathURL("zon.png"), "sun");
        manager.loadFromURL(Utils.getClasspathURL("player.png"), "player");
        manager.loadFromURL(Utils.getClasspathURL("wall.png"), "wall");
    }

    private void createTextureParts() {
        final TexturePartManager manager = TexturePartManager.getInstance();
        manager.createTexturePart("player_eyes", "player", new Rectangle(70,
                96, 20, 32));
        manager.createTexturePart("player_background", "player", new Rectangle(
                96, 0, 96, 96));
        manager.createTexturePart("player_body", "player", new Rectangle(0, 0,
                96, 96));
        manager.createTexturePart("player_background_foot", "player",
                new Rectangle(192, 64, 96, 32));
        manager.createTexturePart("player_foot", "player", new Rectangle(192,
                32, 96, 32));
        manager.createTexturePart("sun", "sun", new Rectangle(0, 0, 128, 128));
        manager.createTexturePart(
                "tile_empty",
                "tiles",
                createMapTextureRectangle(6, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_filled",
                "tiles",
                createMapTextureRectangle(1, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass_end_left",
                "tiles",
                createMapTextureRectangle(4, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass_end_right",
                "tiles",
                createMapTextureRectangle(5, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_grass",
                "tiles",
                createMapTextureRectangle(16, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_left_grass",
                "tiles",
                createMapTextureRectangle(19, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_left_mud",
                "tiles",
                createMapTextureRectangle(20, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_right_mud",
                "tiles",
                createMapTextureRectangle(21, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_left_grass",
                "tiles",
                createMapTextureRectangle(32, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_left_mud",
                "tiles",
                createMapTextureRectangle(36, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_right_mud",
                "tiles",
                createMapTextureRectangle(37, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_top_left_grass_end",
                "tiles",
                createMapTextureRectangle(48, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
        manager.createTexturePart(
                "tile_bottom_mud",
                "tiles",
                createMapTextureRectangle(52, TILES_PER_LINE, TILE_SIZE,
                        TILE_SIZE));
    }

    private Rectangle createMapTextureRectangle(final int tileNumber,
            final int tileNumPerLine, final int tileWidth, final int tileHeight) {
        return new Rectangle((tileNumber % 16 * tileWidth + 1), (tileNumber
                / 16 * tileHeight + 1), (tileWidth - 2), (tileHeight - 2));
    }

    @Override
    public void dispose() {
        if (!quitting) {
            quitting = true;
            renderer.dispose();
            try {
                networkDataWriter.sendLogoutMessage(channel);
            } catch (IOException e) {
                LOG.fatal("IOException during logout", e);
            }
        }
    }
}
