package walledin.engine.gui.components;

import java.awt.event.KeyEvent;

import sun.font.FontManager;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenKeyEvent;
import walledin.engine.gui.ScreenKeyEventListener;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class TextBox extends AbstractScreen implements ScreenKeyEventListener,
        ScreenMouseEventListener {
    private StringBuffer text;
    /** Keeps track if move is hovering over button. */
    private boolean selected;
    private final int maxChar;

    public TextBox(final AbstractScreen parent, final String initialText,
            final Vector2f pos, final float width, final int maxChar) {
        super(parent, new Rectangle(0, -20, width, 30), 2);
        this.text = new StringBuffer(initialText);
        this.maxChar = maxChar;
        setPosition(pos);
        addMouseEventListener(this);
        addKeyEventListener(this);
        show();
    }

    @Override
    public void onMouseDown(ScreenMouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseHover(ScreenMouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMouseClicked(ScreenMouseEvent e) {
        selected = true;
    }

    @Override
    public void update(double delta) {
        selected = false;

        super.update(delta);
    }

    @Override
    public void draw(Renderer renderer) {
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);

        /*
         * if (selected) { renderer.setColorRGB(1, 0, 0); }
         */

        renderer.drawRectOutline(getRectangle());
        font.renderText(renderer, text.toString(), new Vector2f(4, 0));
        renderer.setColorRGB(1, 1, 1);
        super.draw(renderer);
    }

    @Override
    public void onKeyDown(ScreenKeyEvent e) {

    }

    @Override
    public void onKeyUp(ScreenKeyEvent e) {
        for (int key : e.getKeys()) {
            if (key == KeyEvent.VK_BACK_SPACE) {
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                }
            } else {
                if (text.length() < maxChar) {
                    text.append((char) key);
                }
            }
        }
    }

}
