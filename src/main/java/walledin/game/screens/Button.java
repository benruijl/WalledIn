package walledin.game.screens;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class Button extends Screen {
    /** Button text. */
    private String text;
    private boolean selected; // mouse hovering over button?

    public Button(final Screen parent, final Rectangle boundingRect,
            final String text, final Vector2f pos) {
        super(parent, boundingRect);
        this.text = text;
        setPosition(pos);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update(double delta) {
        selected = pointInScreen(Input.getInstance().getMousePos().asVector2f());

        super.update(delta);
    }

    @Override
    public void draw(Renderer renderer) {

        final Font font = getManager().getFont("arial20");
        
        if (selected) {
            renderer.setColorRGB(1, 0, 0);
        }
        
        font.renderText(renderer, text, getPosition());
        renderer.setColorRGB(1, 1, 1);
        super.draw(renderer);
    }

    public void setText(String text) {
        this.text = text;
    }
}
