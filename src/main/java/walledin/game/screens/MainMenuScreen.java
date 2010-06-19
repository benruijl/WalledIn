package walledin.game.screens;

import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.screens.ScreenManager.ScreenType;

public class MainMenuScreen extends Screen {
    Screen startButton;

    public MainMenuScreen(Screen parent) {
        super(parent, null);
    }

    @Override
    public void draw(final Renderer renderer) {

        super.draw(renderer);
    }

    @Override
    public void initialize() {
        startButton = new Button(this, new Rectangle(0, 0, 100, 60),
                "Start game", new Vector2f(300, 100));
        addChild(startButton);
    }

    @Override
    public void update(final double delta) {
        if (getState() == ScreenState.Hidden) {
            return;
        }

        super.update(delta);

        if (startButton.pointInScreen(Input.getInstance().getMousePos()
                .asVector2f())) {
            if (Input.getInstance().getMouseDown()) {
                getManager().getScreen(ScreenType.GAME).initialize();
                getManager().getScreen(ScreenType.GAME).setActive(true);
                setState(ScreenState.Hidden); // hide main menu
            }
        }

    }

}
