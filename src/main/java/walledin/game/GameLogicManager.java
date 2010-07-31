package walledin.game;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.SpawnPoint;
import walledin.game.network.server.Server;
import walledin.util.SettingsManager;
import walledin.util.Utils;

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
        private Teams team;

        public PlayerClientInfo(final String entityName, final Teams team) {
            this.entityName = entityName;
            this.team = team;
        }

        public PlayerClientInfo(final String entityName) {
            this.entityName = entityName;
        }

        public String getEntityName() {
            return entityName;
        }

        public Teams getTeam() {
            return team;
        }

        public void setTeam(final Teams team) {
            this.team = team;
        }
    }

    /** This class contains all information about the player. */
    public final class PlayerInfo {
        private float currentRespawnTime;
        private final Entity player;
        private boolean dead;
        private boolean respawn;
        private Teams team;

        public PlayerInfo(final Entity player) {
            super();
            this.player = player;
            dead = false;
            respawn = false;
            team = Teams.UNSELECTED;
        }

        public Teams getTeam() {
            return team;
        }

        public void setTeam(final Teams team) {
            this.team = team;
        }

        public Entity getPlayer() {
            return player;
        }

        public boolean isDead() {
            return dead;
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
                player.remove(); // remove the player
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
    private final Map<Teams, Set<PlayerInfo>> teams;
    /** Respawn time in seconds. */
    private final float respawnTime;
    /** Current game mode. */
    private GameMode gameMode;

    public GameLogicManager(final Server server) {
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        players = new HashMap<String, PlayerInfo>();
        teams = new HashMap<Teams, Set<PlayerInfo>>();

        /* Initialize the map */
        for (final Teams team : Teams.values()) {
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
    }

    public final Server getServer() {
        return server;
    }

    public final EntityManager getEntityManager() {
        return entityManager;
    }

    public final EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    /**
     * Gets the game mode.
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
    public final void setTeam(final String entityName, final Teams team) {
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
    public final void spawnPlayer(final Entity player) {
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
    public final Entity createPlayer(final String entityName, final String name) {
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
    public final void removePlayer(final String entityName) {
        /* If the player is dead, he is already removed from the entity list. */
        if (!players.get(entityName).isDead()) {
            entityManager.remove(entityName);
        }

        players.remove(entityName);
    }

    /**
     * Update the gamestate, removes disconnected players and does collision
     * detection.
     * 
     * @param delta
     *            Time elapsed since last update
     */
    public final void update(final double delta) {
        /* Update all entities */
        entityManager.update(delta);

        /* Update the players */
        for (final PlayerInfo info : players.values()) {
            info.update(delta);

            if (info.shouldRespawn()) {
                spawnPlayer(info.getPlayer());
                entityManager.add(info.getPlayer());
                info.hasRespawned();
            }

        }

        /* Do collision detection */
        entityManager.doCollisionDetection(map, delta);
    }

    public final void initialize() {
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
    }

}
