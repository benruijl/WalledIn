package walledin.network;

import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.RenderListener;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.game.ClientEntityFactory;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.map.GameMapIO;
import walledin.game.map.GameMapIOXML;
import walledin.math.Rectangle;
import walledin.math.Vector2f;

public class Client implements RenderListener, Runnable {
	private static final int TILE_SIZE = 64;
	private static final int TILES_PER_LINE = 16;
	private Font font;
	private Renderer renderer; // current renderer
	private EntityManager entityManager;
	private Entity map;
	private SocketAddress host;
	private String username;
	
	public static void main(String[] args) {
		Renderer renderer = new Renderer();
		Client client = new Client(renderer);
		// Start client
		Thread thread = new Thread(client, "client");
		thread.start();
		// Start renderer
		renderer.initialize("WalledIn");
		renderer.addListener(client);
		renderer.beginLoop();
	}

	/**
	 * Create the client
	 * @param renderer Current renderer
	 */
	public Client(Renderer renderer) {
		this.renderer = renderer;
		entityManager = new EntityManager(new ClientEntityFactory());
		// Hardcode the host and username for now
		host = new InetSocketAddress("localhost", 1234);
		username = "BLAA";
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(final double delta) {
		/* Update all entities */
		entityManager.update(delta);
		
		/* Spawn bullets if key pressed */
		if (Input.getInstance().keyDown(KeyEvent.VK_ENTER))
		{
			Entity player = entityManager.get("Player01");
			Vector2f playerPosition = player.getAttribute(Attribute.POSITION);
			int or = player.getAttribute(Attribute.ORIENTATION);
			Vector2f position = playerPosition.add(new Vector2f(or * 50.0f,
					20.0f));
			Vector2f velocity = new Vector2f(or * 400.0f, 0);
			
			Entity bullet = entityManager.create("bullet", entityManager.generateUniqueName("bullet"));
			bullet.setAttribute(Attribute.POSITION, position);
			bullet.setAttribute(Attribute.VELOCITY, velocity);
			
			entityManager.add(bullet);
			
			Input.getInstance().setKeyUp(KeyEvent.VK_ENTER);
		}
		
		/* Center the camera around the player */
		// TODO get player entity back from server so we know what to center on
		renderer.centerAround((Vector2f) entityManager.get("Player01").getAttribute(
				Attribute.POSITION));

		/* Toggle full screen, current not working correctly */
		if (Input.getInstance().keyDown(KeyEvent.VK_F1)) {
			renderer.toggleFullScreen();
			Input.getInstance().setKeyUp(KeyEvent.VK_F1);
		}
	}
	
	@Override
	public void draw(final Renderer renderer) {
		entityManager.draw(renderer); // draw all entities in correct order

		/* Render current FPS */
		renderer.startHUDRendering();
		font.renderText(renderer, "FPS: " + Float.toString(renderer.getFPS()), new Vector2f(600, 20));
		renderer.stopHUDRendering();
	}

	/**
	 * Initialize game
	 */
	@Override
	public void init() {
		loadTextures();
		createTextureParts();

		font = new Font(); // load font
		font.readFromFile("data/arial20.font");

		// initialize entity manager
		entityManager.init();

		final GameMapIO mMapIO = new GameMapIOXML(entityManager); // choose XML as format
		
		map = mMapIO.readFromFile("data/map.xml");
		entityManager.create("Background", "Background");
	}

	private void loadTextures() {
		final TextureManager manager = TextureManager.getInstance();
		manager.loadFromFile("data/tiles.png", "tiles");
		manager.loadFromFile("data/zon.png", "sun");
		manager.loadFromFile("data/player.png", "player");
		manager.loadFromFile("data/wall.png", "wall");
		manager.loadFromFile("data/game.png", "game");
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
		manager.createTexturePart("tile_empty", "tiles",
				createMapTextureRectangle(6, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_filled", "tiles",
				createMapTextureRectangle(1, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass_end_left", "tiles",
				createMapTextureRectangle(4, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass_end_right", "tiles",
				createMapTextureRectangle(5, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_grass", "tiles",
				createMapTextureRectangle(16, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_left_grass", "tiles",
				createMapTextureRectangle(19, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_left_mud", "tiles",
				createMapTextureRectangle(20, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_right_mud", "tiles",
				createMapTextureRectangle(21, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_left_grass", "tiles",
				createMapTextureRectangle(32, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_left_mud", "tiles",
				createMapTextureRectangle(36, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_right_mud", "tiles",
				createMapTextureRectangle(37, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_top_left_grass_end", "tiles",
				createMapTextureRectangle(48, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
		manager.createTexturePart("tile_bottom_mud", "tiles",
				createMapTextureRectangle(52, TILES_PER_LINE, TILE_SIZE,
						TILE_SIZE));
	}

	private Rectangle createMapTextureRectangle(final int tileNumber,
			final int tileNumPerLine, final int tileWidth, final int tileHeight) {
		return new Rectangle((tileNumber % 16 * tileWidth + 1), (tileNumber
				/ 16 * tileHeight + 1), (tileWidth - 2), (tileHeight - 2));
	}
}
