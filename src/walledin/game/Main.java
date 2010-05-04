package walledin.game;

import walledin.engine.Renderer;

public class Main {
	private final Game game;
	private final Renderer renderer;

	public Main() {
		renderer = new Renderer();
		game = new Game();
	}

	public void run() {
		renderer.initialize("WalledIn");
		renderer.addListener(game);
		renderer.beginLoop();
	}

	public static void main(final String[] args) {
		final Main main = new Main();
		main.run();
	}
}
