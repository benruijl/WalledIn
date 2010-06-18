package walledin.game.screens;

import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;
import walledin.game.screens.ScreenManager.ScreenType;

public class MainMenuScreen extends Screen {
    
    public MainMenuScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void draw(final Renderer renderer) {

        super.draw(renderer);
    }

    @Override
    public void initialize() {
        Screen startButton = new Button(this, "Start game", new Vector2f(300,
                100));
        addChild(startButton);
    }

    @Override
    public void update(final double delta) {
        super.update(delta);

        if (Input.getInstance().getMouseDown()) {
            getManager().getScreen(ScreenType.GAME).setActive(true);
            setState(ScreenState.Hidden); // hide main menu
        }

    }

}
