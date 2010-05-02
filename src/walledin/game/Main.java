package walledin.game;

import walledin.engine.Renderer;

public class Main {
	private final Game mGame;
	private final Renderer renderer;

	public Main() {
		renderer = new Renderer();
		mGame = new Game();
	}

	public void run() {
		renderer.initialize("WalledIn");
		renderer.addListener(mGame);
		renderer.beginLoop();
	}

	public static void main(final String[] args) {
		final Main main = new Main();
		main.run();
	}
}
