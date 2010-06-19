package walledin.game.screens;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class Button extends Screen {
    /** Button text. */
    private String text;

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
    public void draw(Renderer renderer) {

        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, text, getPosition());

        super.draw(renderer);
    }

    public void setText(String text) {
        this.text = text;
    }
}
