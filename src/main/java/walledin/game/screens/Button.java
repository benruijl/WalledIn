package walledin.game.screens;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.math.Vector2f;

public class Button extends Screen {
    /** Button text. */
    private String text;
    private Vector2f pos;
    
    public Button(Screen parent) {
        super(parent);
    }

    public Button(Screen parent, String text, Vector2f pos) {
        super(parent);
        this.text = text;
        this.pos = pos;
    }

    @Override
    public void initialize() {
        
    }
    
    @Override
    public void draw(Renderer renderer) {
        
        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, text, pos);
        
        super.draw(renderer);
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setPos(Vector2f pos) {
        this.pos = pos;
    }

}
