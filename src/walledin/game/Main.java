package walledin.game;

// local includes
import walledin.engine.Renderer;

public class Main {
	Game mGame;

	public Main() {
		final Renderer renderer = new Renderer();
		mGame = new Game();

		renderer.initialize("WalledIn");
		renderer.addListener(mGame);
		renderer.beginLoop();
	}

	public static void main(final String[] args) {
		final Main game = new Main();

	}
}
