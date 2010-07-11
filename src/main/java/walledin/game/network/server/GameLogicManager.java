package walledin.game.network.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.EntityFactory;
import walledin.game.entity.Family;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.game.map.SpawnPoint;
import walledin.util.Utils;

public class GameLogicManager {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(GameLogicManager.class);

    private final Server server;
    /** Random number generator. */
    private Random rng;
    private final EntityManager entityManager;
    private final EntityFactory entityFactory;

    /* Special entities */
    private Entity map;
    private List<Entity> players;

    public GameLogicManager(Server server) {
        entityFactory = new EntityFactory();
        entityManager = new EntityManager(entityFactory);
        players = new ArrayList<Entity>();
        this.server = server;
        this.rng = new Random();
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
    }

    /**
     * Creates a player and spawns it.
     * 
     * @param entityName
     *            Unique entity name
     * @param name
     *            In-game name of this player
     * @return Player entity
     */
    public Entity createPlayer(final String entityName, final String name) {
        final Entity player = entityManager.create(Family.PLAYER, entityName);
        player.setAttribute(Attribute.PLAYER_NAME, name);
        spawnPlayer(player);

        return player;
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
