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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.math.Vector2f;
import walledin.engine.math.Vector2i;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.SpawnPoint;
import walledin.game.map.Tile;
import walledin.game.network.server.Server;
import walledin.util.SettingsManager;
import walledin.util.Utils;

/**
 * This class takes care of the game logic.
 * 
 * @author Ben Ruijl
 * 
 */
public final class GameLogicManager {
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

    /**
     * This class contains all information the client should know about the
     * player.
     */
    public static final class PlayerClientInfo {
        private final String entityName;
        private Team team;

        public PlayerClientInfo(final String entityName, final Team team) {
            this.entityName = entityName;
            this.team = team;
        }

        public PlayerClientInfo(final String entityName) {
            this.entityName = entityName;
        }

        public String getEntityName() {
            return entityName;
        }

        public Team getTeam() {
            return team;
        }

        public void setTeam(final Team team) {
            this.team = team;
        }
    }

    /** This class contains all information about the player. */
    public final class PlayerInfo {
        private float currentRespawnTime;
        private final Entity player;
        private boolean dead;
        private boolean respawn;
        private Team team;
        private float walledInTime;

        public PlayerInfo(final Entity player) {
            super();
            this.player = player;
            dead = false;
            respawn = false;
            team = Team.UNSELECTED;
        }

        public Team getTeam() {
            return team;
        }

        public void setTeam(final Team team) {
            this.team = team;
        }

        public Entity getPlayer() {
            return player;
        }

        public boolean isDead() {
            return dead;
        }

        public void setWalledInTime(final float walledInTime) {
            this.walledInTime = walledInTime;
        }

        public float getWalledInTime() {
            return walledInTime;
        }

        /**
         * Should be called when the player has been respawned.
         */
        public void hasRespawned() {
            dead = false;
            respawn = false;
        }

        /**
         * Checks if the player died and determines if the player should be
         * respawned.
         * 
         * @param delta
         *            Delta time since last update
         */
        public void update(final double delta) {
            /* Check if the player died */
            if (!dead && (Integer) player.getAttribute(Attribute.HEALTH) == 0) {
                dead = true;
                respawn = false;
            }

            if (dead && !respawn) {
                currentRespawnTime += delta;

                if (currentRespawnTime > respawnTime) {
                    currentRespawnTime = 0;
                    respawn = true;
                }
            }
        }

        public boolean shouldRespawn() {
            return respawn;
        }

    }

    /** Active map. */
    private Entity map;
    /** A map from the player entity name to the player info. */
    private final Map<String, PlayerInfo> players;
    /** A map to from a team to the players in it. */
    private final Map<Team, Set<PlayerInfo>> teams;
    /** Respawn time in seconds. */
    private final float respawnTime;
    /** Current game mode. */
    private final GameMode gameMode;

    /* Walled In checks */
    /** Mobility field of the map. */
    private boolean[][] staticField;
    /** Maximum Walled In time. */
    private final float maxWalledInTime;
    /** Minimum Walled In space in player size units. */
    private final int minimalWalledInSpace;

    public GameLogicManager(final Server server) {
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        players = new HashMap<String, PlayerInfo>();
        teams = new HashMap<Team, Set<PlayerInfo>>();

        /* Initialize the map */
        for (final Team team : Team.values()) {
            teams.put(team, new HashSet<GameLogicManager.PlayerInfo>());
        }

        this.server = server;

        /* Initialize random number generator */
        rng = new Random();

        /* Load settings. */
        respawnTime = SettingsManager.getInstance()
                .getFloat("game.respawnTime");
        gameMode = GameMode.valueOf(SettingsManager.getInstance().getString(
                "game.gameMode"));
        maxWalledInTime = SettingsManager.getInstance().getFloat(
                "game.walledInTime");
        minimalWalledInSpace = SettingsManager.getInstance().getInteger(
                "game.mininmalWalledInSpace");
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
     * Removes a player from the player list and from the entity list.
     * 
     * @param entityName
     *            Player name
     */
    public void removePlayer(final String entityName) {
        if (!players.containsKey(entityName)) {
            return;
        }
        
        /* If the player is dead, he is already removed from the entity list. */
        if (!players.get(entityName).isDead()) {
            entityManager.remove(entityName);
        }

        players.get(entityName).getPlayer().sendMessage(MessageType.DEATH, null);
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
        final List<Tile> tiles = (List<Tile>) map.getAttribute(Attribute.TILES);

        /*
         * Mark the filled tiles. TODO: if the tile width is greater than the
         * player size, multiple entries in the field should be set.
         */
        for (final Tile tile : tiles) {
            if (tile.getType().isSolid()) {
                staticField[(int) (tile.getX() * tileWidth / playerSize)][(int) (tile
                        .getY() * tileWidth / playerSize)] = true;
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
                removePlayer(info.getPlayer().getName());
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

        /* Do collision detection */
        entityManager.doCollisionDetection(map, delta);
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

        /* Build the static movability field. */
        buildStaticField();
    }

}
