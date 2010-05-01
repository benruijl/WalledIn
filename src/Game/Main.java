package Game;

// local includes
import walledin.Renderer;

public class Main {
    Game mGame;

    public Main() {
        Renderer renderer = new Renderer();
        mGame = new Game();

        renderer.initialize("WalledIn");
        renderer.addListener(mGame);
        renderer.beginLoop();
    }



    public static void main(String[] args) {
        Main game = new Main();

    }
}
