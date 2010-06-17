package walledin.game.screens;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;

public class MainMenuScreen extends Screen {

    @Override
    public void draw(Renderer renderer) {
        Font font = getManager().getFont("arial20"); 
        font.renderText(renderer, "Main menu!",
                new Vector2f(300, 100));

    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(double delta) {
        if (Input.getInstance().getMouseDown()) {
            getManager().getScreen(ScreenType.GAME).setActive(true);
            setState(ScreenState.Hidden); // hide main menu
        }

    }

}
