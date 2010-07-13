package walledin.game.network.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.SpawnPoint;
import walledin.util.SettingsManager;
import walledin.util.Utils;

public class GameLogicManager {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GameLogicManager.class);

    private final Server server;
    /** Random number generator. */
    private Random rng;
    private final EntityManager entityManager;
    private final EntityFactory entityFactory;

    public class PlayerInfo {
        private float currentRespawnTime;
        private Entity player;
        private boolean dead;
        private boolean respawn;

        public PlayerInfo(Entity player) {
            super();
            this.player = player;
            this.dead = false;
            this.respawn = false;
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
        public void update(double delta) {
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

    /* Special entities */
    private Entity map;
    private Map<String, PlayerInfo> players;
    /** Respawn time in seconds */
    private float respawnTime;

    public GameLogicManager(Server server) {
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        players = new HashMap<String, PlayerInfo>();
        this.server = server;
        this.rng = new Random();

        LOG.info(SettingsManager.getInstance().getString("game.respawnTime"));
        respawnTime = SettingsManager.getInstance()
                .getFloat("game.respawnTime");
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
     * Spawns a player at a random spawn point. The player is <b>not</b> added
     * to the entity list.
     * 
     * @param player
     *            Player
     */
    public void spawnPlayer(Entity player) {
        List<SpawnPoint> points = (List<SpawnPoint>) map
                .getAttribute(Attribute.SPAWN_POINTS);

        // randomly choose a point to spawn the player
        SpawnPoint p = points.get(rng.nextInt(points.size()));
        player.setAttribute(Attribute.POSITION, p.getPos());
        player.sendMessage(MessageType.RESTORE_HEALTH, 100); // FIXME
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
        for (PlayerInfo info : players.values()) {
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
        map = mapIO
                .readFromURL(entityManager, Utils.getClasspathURL("map.xml"));

        // this name will be sent to the client
        map.setAttribute(Attribute.MAP_NAME, "map.xml");
    }

}
