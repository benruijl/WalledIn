package walledin.engine.gui.components;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class Slider extends AbstractScreen implements ScreenMouseEventListener {
    private final float width = 80.0f;
    private final float height = 30.0f;

    private final List<String> sliderValues;
    private int currentIndex;

    public Slider(final AbstractScreen parent, final Rectangle boudingRect,
            final int z, final Vector2f pos) {
        super(parent, boudingRect, z);

        sliderValues = new ArrayList<String>();
        currentIndex = -1;
        setPosition(pos);
        addMouseEventListener(this);
        show();
    }

    public void addValue(final String value) {
        sliderValues.add(value);

        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    @Override
    public void draw(final Renderer renderer) {

        renderer.drawFilledRect(new Rectangle(0, 0, 5, height));
        renderer.drawFilledRect(new Rectangle(width, 0, 5, height));

        if (sliderValues.size() > 1) {
            renderer.setColorRGB(1, 0, 0);
            renderer.drawFilledRect(new Rectangle(currentIndex
                    / (float) (sliderValues.size() - 1) * width, 0, 8, height));
            renderer.setColorRGB(1, 1, 1);

            getManager().getFont(FontType.BUTTON_CAPTION).renderText(renderer,
                    sliderValues.get(currentIndex),
                    new Vector2f(width + 40, height / 1.5f));
        }

        super.draw(renderer);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
        if (new Rectangle(0, 0, 5, height).translate(getAbsolutePosition())
                .containsPoint(e.getPos()) && currentIndex > 0) {
            currentIndex--;
        }

        if (new Rectangle(width, 0, 5, height).translate(getAbsolutePosition())
                .containsPoint(e.getPos())
                && currentIndex < sliderValues.size() - 1) {
            currentIndex++;
        }
    }

}
