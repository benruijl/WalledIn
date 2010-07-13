package walledin.game.gui;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.gui.components.Button;

public class PopupDialog extends Screen implements ScreenMouseEventListener {
    private final String text;
    private Button okButton;

    public PopupDialog(final ScreenManager manager, final String text) {
        super(manager, new Rectangle(-10, -20, manager.getFont("arial20")
                .getTextWidth(text) + 20, 70));
        setPosition(new Vector2f(300, 250));
        this.text = text;
        System.out.print(getParent());
    }

    public PopupDialog(final Screen parent, final String text) {
        super(parent, new Rectangle(-10, -20, parent.getManager().getFont(
                "arial20").getTextWidth(text) + 20, 70));
        setPosition(new Vector2f(300, 250));
        this.text = text;
        System.out.print(getParent());
    }

    @Override
    public final void draw(final Renderer renderer) {
        renderer.setColorRGB(0.4f, 0.4f, 0.4f);
        renderer.drawFilledRect(getRectangle().translate(getPosition()));
        renderer.setColorRGB(1.0f, 1.0f, 1.0f);

        getManager().getFont("arial20").renderText(renderer, text,
                new Vector2f(300, 250));

        super.draw(renderer);
    }

    @Override
    public final void initialize() {
        okButton = new Button(this, "OK", new Vector2f(getRectangle()
                .getWidth()
                / 2.0f
                + getPosition().getX()
                - getManager().getFont("arial20").getTextWidth("OK") / 2.0f,
                getPosition().getY() + getRectangle().getHeight() - 25));
        okButton.addMouseEventListener(this);
        addChild(okButton);
    }

    @Override
    public final void onMouseDown(final ScreenMouseEvent e) {
        if (e.getScreen() == okButton) {
            dispose();
        }

    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
        // TODO Auto-generated method stub

    }

}
