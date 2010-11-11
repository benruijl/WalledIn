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
package walledin.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.engine.math.Vector2i;
import walledin.engine.physics.PhysicsManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.game.gamemode.GameMode;
import walledin.game.gamemode.GameModeHandler;
import walledin.game.gamemode.GameModeHandlerFactory;
import walledin.game.gamemode.GameStateListener;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.SpawnPoint;
import walledin.game.map.Tile;
import walledin.game.network.server.Server;
import walledin.util.SettingsManager;
import walledin.util.Utils;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * This class takes care of the game logic.
 * 
 * @author Ben Ruijl
 * 
 */
public final class GameLogicManager implements GameStateListener,
        EntityUpdateListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GameLogicManager.class);

    /** Server that is running this game. */
    private final Server server;
    /** Random number generator. */
    private final Random rng;
    /** Entity manager. */
    private final EntityManager entityManager;
    /** Entity factory. */
    private final EntityFactory entityFactory;

    /** Active map. */
    private Entity map;
    /** A map from the player entity name to the player info. */
    private final Map<String, PlayerInfo> players;
    /** A map to from a team to the players in it. */
    private final Map<Team, Set<PlayerInfo>> teams;
    /** Current game mode. */
    private final GameMode gameMode;
    /** Current game mode handler. */
    private final GameModeHandler gameModeHandler;

    /* Walled In checks */
    /** Mobility field of the map. */
    private boolean[][] staticField;
    /** Maximum Walled In time. */
    private final float maxWalledInTime;
    /** Minimum Walled In space in player size units. */
    private final int minimalWalledInSpace;

    public GameLogicManager() {
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        entityManager.addListener(this);
        players = new HashMap<String, PlayerInfo>();
        teams = new HashMap<Team, Set<PlayerInfo>>();

        /* Initialize the map */
        for (final Team team : Team.values()) {
            teams.put(team, new HashSet<PlayerInfo>());
        }

        server = new Server(this);

        /* Initialize random number generator */
        rng = new Random();

        gameMode = GameMode.valueOf(SettingsManager.getInstance().getString(
                "game.gameMode"));
        maxWalledInTime = SettingsManager.getInstance().getFloat(
                "game.walledInTime");
        minimalWalledInSpace = SettingsManager.getInstance().getInteger(
                "game.mininmalWalledInSpace");

        gameModeHandler = GameModeHandlerFactory.createHandler(gameMode, this);
    }

    /**
     * Start of application. It runs the server.
     * 
     * @param args
     *            Command line arguments
     * @throws IOException
     */
    public static void main(final String[] args) {
        /* First load the settings file */
        try {
            SettingsManager.getInstance().loadSettings(
                    Utils.getClasspathURL("server_settings.ini"));
        } catch (final IOException e) {
            LOG.error("Could not read configuration file.", e);
        }

        new GameLogicManager().run();
    }

    /**
     * Runs the server.
     */
    private void run() {
        try {
            server.run();
        } catch (final IOException e) {
            LOG.fatal("A fatal network error occured.", e);
        }
    }

    public Server getServer() {
        return server;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * Gets the game mode.
     * 
     * @return Current game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Registers the player with a certain team.
     * 
     * @param entityName
     *            Player entity name
     * @param team
     *            new team
     */
    public void setTeam(final String entityName, final Team team) {
        final PlayerInfo info = players.get(entityName);

        /* Unregister from previous team */
        if (info.getTeam() != null) {
            teams.get(info.getTeam()).remove(info);
        }

        teams.get(team).add(info);
        info.setTeam(team);
    }

    /**
     * Spawns a player at a random spawn point. The player is <b>not</b> added
     * to the entity list.
     * 
     * @param player
     *            Player
     */
    public void spawnPlayer(final Entity player) {
        @SuppressWarnings("unchecked")
        final List<SpawnPoint> points = (List<SpawnPoint>) map
                .getAttribute(Attribute.SPAWN_POINTS);

        // randomly choose a point to spawn the player
        final SpawnPoint p = points.get(rng.nextInt(points.size()));
        player.setAttribute(Attribute.POSITION, p.getPos());
        player.sendMessage(MessageType.RESTORE_HEALTH, 100); // FIXME
    }

    /**
     * Returns a map from player name to player info for all players.
     * 
     * @return Map
     */
    public Map<String, PlayerInfo> getPlayers() {
        return players;
    }

    /**
     * Creates a player and spawns it. It adds the player to a list.
     * 
     * @param entityName
     *            Unique entity name
     * @param name
     *            In-game name of the player
     * @return Player entity
     */
    public Entity createPlayer(final String entityName, final String name) {
        final Entity player = entityManager.create(Family.PLAYER, entityName);
        player.setAttribute(Attribute.PLAYER_NAME, name);
        players.put(entityName, new PlayerInfo(player));
        spawnPlayer(player);

        return player;
    }

    /**
     * Kills a player by removing him from the entity list and by sending the
     * death event.
     * 
     * @param entityName
     *            Player name
     */
    public void killPlayer(final String entityName) {
        if (!players.containsKey(entityName)) {
            LOG.info("Tried to remove player that is not in the list.");
            return;
        }

        /* If the player is dead, he is already removed from the entity list. */
        if (!players.get(entityName).isDead()) {
            entityManager.remove(entityName);
        }

        players.get(entityName).getPlayer()
                .sendMessage(MessageType.DEATH, null);
    }

    /**
     * Removes a player from the player list and from the entity list.
     * 
     * @param entityName
     *            Player name
     */
    public void removePlayer(final String entityName) {
        if (!players.containsKey(entityName)) {
            LOG.info("Tried to remove player that is not in the list.");
            return;
        }

        killPlayer(entityName);
        players.remove(entityName);
    }

    /**
     * Recursively checks if a certain distance can be reached from a starting
     * position.
     * 
     * @param distance
     *            Distance to reach
     * @param curPos
     *            Current position
     * @param startPos
     *            Starting position
     * @param field
     *            Field of booleans. True is filled and false is empty.
     * @return True if a certain distance can be reached, else false.
     */
    boolean canReachDistance(final int distance, final Vector2i curPos,
            final Vector2i startPos, final boolean[][] field) {

        if (field[curPos.getX()][curPos.getY()]) {
            return false;
        }

        /* Disables going back. */
        field[curPos.getX()][curPos.getY()] = true;

        if (curPos.sub(startPos).lengthSquared() >= distance * distance) {
            return true;
        }

        /* Crawl through the level. */
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == j || i == -j || curPos.getX() + i < 0
                        || curPos.getY() + j < 0
                        || curPos.getX() + i >= field.length
                        || curPos.getY() + j >= field[0].length) {
                    continue;
                }

                if (canReachDistance(distance, curPos.add(new Vector2i(i, j)),
                        startPos, field)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * Builds the mobility field by looking only at the map.
     */
    private void buildStaticField() {
        /* Do a + 1 do avoid rounding errors */
        final float width = (Integer) map.getAttribute(Attribute.WIDTH) + 1;
        final float height = (Integer) map.getAttribute(Attribute.HEIGHT) + 1;
        final float playerSize = 44; // FIXME: hardcoded
        final float tileWidth = (Float) map.getAttribute(Attribute.TILE_WIDTH);

        staticField = new boolean[(int) (width * tileWidth / playerSize)][(int) (height
                * tileWidth / playerSize)];
        @SuppressWarnings("unchecked")
        final List<Tile> tiles = (List<Tile>) map.getAttribute(Attribute.TILES);

        /*
         * Mark the filled tiles. TODO: if the tile width is greater than the
         * player size, multiple entries in the field should be set.
         */
        for (final Tile tile : tiles) {
            if (tile.getType().isSolid()) {
                staticField[(int) (tile.getX() * tileWidth / playerSize)][(int) (tile
                        .getY() * tileWidth / playerSize)] = true;

                CollisionShape tileShape = new BoxShape(new Vector3f(tileWidth / 2.0f,
                        tileWidth / 2.0f, 2));
                DefaultMotionState tileMotionState = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                                new Vector3f(tile.getX() * tileWidth, tile
                                        .getY() * tileWidth, 0), 1)));
                RigidBodyConstructionInfo tileRigidBodyCI = new RigidBodyConstructionInfo(
                        0, tileMotionState, tileShape, new Vector3f(0, 0, 0));
                RigidBody tileRigidBody = new RigidBody(tileRigidBodyCI);
                tileRigidBody.setUserPointer(map.getName());

                PhysicsManager.getInstance().getWorld()
                        .addRigidBody(tileRigidBody);
            }
        }

    }

    /**
     * Checks if a certain player is walled in. This is the case if the mobility
     * of the player is less than <code>minimalWalledInSpace</code>.
     * 
     * @param player
     *            Player
     * @return True if walled in, else false.
     */
    private boolean detectWalledIn(final Entity player) {
        final float playerSize = 44; // FIXME: hardcoded
        final Vector2f playerPos = (Vector2f) player
                .getAttribute(Attribute.POSITION);

        /* Use the static field as a base for new field. */
        final boolean[][] field = Utils.clone2DArray(staticField);

        /* Check the foam particles. */
        for (final Entity ent : entityManager.getEntities().values()) {
            if (ent.getFamily() == Family.FOAM_PARTICLE) {
                final Vector2f pos = (Vector2f) ent
                        .getAttribute(Attribute.POSITION);
                field[(int) (pos.getX() / playerSize)][(int) (pos.getY() / playerSize)] = true;
            }
        }

        /* Make the player position free. */
        field[(int) (playerPos.getX() / playerSize)][(int) (playerPos.getY() / playerSize)] = false;

        /* Output the map if tracing. */
        if (LOG.getLevel() == Level.TRACE) {
            outputMobilityMap(field);
        }

        if (!canReachDistance(minimalWalledInSpace,
                playerPos.scale(1 / playerSize).asVector2i(),
                playerPos.scale(1 / playerSize).asVector2i(), field)) {
            return true;
        }

        return false;
    }

    /**
     * Outputs a mobility map. Useful for debugging.
     * 
     * @param field
     *            Mobility map
     */
    private void outputMobilityMap(final boolean[][] field) {
        try {
            final FileWriter fstream = new FileWriter("mobmap.txt");
            final BufferedWriter out = new BufferedWriter(fstream);

            // print map
            for (int j = 0; j < field[0].length; j++) {
                for (final boolean[] element : field) {
                    if (element[j]) {
                        out.write("#");
                    } else {
                        out.write(" ");
                    }
                }
                out.write("\n");
            }

            out.close();

        } catch (final IOException e) {
            LOG.error("Error while writing mobility map: ", e);
        }
    }

    /**
     * Update the gamestate, removes disconnected players and does collision
     * detection.
     * 
     * @param delta
     *            Time elapsed since last update
     */
    public void update(final double delta) {
        /* Update all entities */
        entityManager.update(delta);

        /* Update the players */
        for (final PlayerInfo info : players.values()) {
            info.update(delta);

            if (info.isDead()) {
                killPlayer(info.getPlayer().getName());

                final String killerName = (String) info.getPlayer()
                        .getAttribute(Attribute.LAST_DAMAGE);

                if (killerName != null
                        && !killerName.equals(info.getPlayer().getName())) {
                    /* Add points to the killer of this player. */
                    for (final PlayerInfo killerInfo : players.values()) {
                        if (killerInfo.getPlayer().getName().equals(killerName)) {
                            killerInfo.increaseKillCount();
                        }
                    }
                }
            }

            if (info.shouldRespawn()) {
                spawnPlayer(info.getPlayer());
                entityManager.add(info.getPlayer());
                info.hasRespawned();
            }

            /* Check if walledin */
            if (!info.isDead() && detectWalledIn(info.getPlayer())) {
                info.setWalledInTime(info.getWalledInTime() + (float) delta);

                /* Kill the player if the max walledin time has passed. */
                if (info.getWalledInTime() >= maxWalledInTime) {
                    info.getPlayer().setAttribute(Attribute.HEALTH, 0);
                }
            } else {
                info.setWalledInTime(0);
            }

            info.getPlayer().setAttribute(Attribute.WALLEDIN_IN,
                    info.getWalledInTime() / maxWalledInTime);
        }

        /* Update the game mode specific routines */
        gameModeHandler.update(delta);

        /* Update physics and do collision detection */
        PhysicsManager.getInstance().update();
    }

    /**
     * Initialize the game logic of the server.
     */
    public void initialize() {
        try {
            entityFactory.loadScript(Utils
                    .getClasspathURL("entities/entities.groovy"));
            entityFactory.loadScript(Utils
                    .getClasspathURL("entities/serverentities.groovy"));
        } catch (final CompilationFailedException e) {
            LOG.fatal("Could not compile script", e);
        } catch (final IOException e) {
            LOG.fatal("IOException during loading of scripts", e);
        }
        // initialize entity manager
        entityManager.init();

        final GameMapIO mapIO = new GameMapIOXML(); // choose XML as format
        final String mapName = SettingsManager.getInstance().getString(
                "game.map.name");
        map = mapIO.readFromURL(entityManager, Utils.getClasspathURL(mapName));

        // this name will be sent to the client
        map.setAttribute(Attribute.MAP_NAME, mapName);

        /* Initialize physics and create physical world. */
        // TODO: calculate from map
        PhysicsManager.getInstance().initialize(
                new Rectangle(0, 0, 64 * 32, 48 * 32));

        /* Add a generic contact resolver. */
        PhysicsManager.getInstance().addListener(
                new ContactHandler(entityManager));

        /*
         * Build the static movability field and add static bodies to the
         * physics manager.
         */
        buildStaticField();

    }

    @Override
    public void onGameOver() {
    }

    @Override
    public void onMatchOver() {
        LOG.info("The match has ended.");
    }

    @Override
    public void onEntityCreated(final Entity entity) {
    }

    @Override
    public void onEntityRemoved(final Entity entity) {
    }

    @Override
    public void onEntityUpdated(final Entity entity) {
        // do nothing
    }
}
