package walledin.engine.gui.components;

import org.apache.log4j.Logger;

import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class ScrollBar extends AbstractScreen implements
        ScreenMouseEventListener {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ScrollBar.class);
    private static final float WIDTH = 20f;
    private static final float BUTTON_HEIGHT = 20f;
    private static final float SCROLL_DELAY = 0.1f;

    private int currentIndex;
    private int numEntries;
    private float currentWaitTime;

    private final Rectangle buttonUpRect;
    private final Rectangle buttonDownRect;
    private final int maxVisibleEntries;

    public ScrollBar(AbstractScreen parent, int z, int maxVisibleEntries) {
        super(parent, new Rectangle(0, 0, WIDTH, parent.getRectangle()
                .getBottom()), z);

        setPosition(new Vector2f(parent.getRectangle().getRight() - WIDTH,
                parent.getRectangle().getTop()));

        buttonUpRect = new Rectangle(0, 0, WIDTH, BUTTON_HEIGHT);
        buttonDownRect = new Rectangle(0, getRectangle().getBottom()
                - BUTTON_HEIGHT, WIDTH, BUTTON_HEIGHT);

        this.maxVisibleEntries = maxVisibleEntries;
        currentWaitTime = 0f;
        addMouseEventListener(this);
    }

    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;

        if (currentIndex >= numEntries) {
            currentIndex = numEntries - 1;
        }

        if (numEntries > 0 && currentIndex < 0) {
            currentIndex = 0;
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void update(double delta) {
        if (currentWaitTime < SCROLL_DELAY) {
            currentWaitTime += delta;
        }

        super.update(delta);
    }

    @Override
    public void draw(Renderer renderer) {
        renderer.drawFilledRect(buttonUpRect);
        renderer.drawFilledRect(buttonDownRect);

        renderer.drawRectOutline(getRectangle());

        if (currentIndex >= 0 && numEntries - maxVisibleEntries > 0) {
            renderer.setColorRGB(1, 0, 0);
            renderer.drawFilledRect(new Rectangle(
                    0,
                    buttonUpRect.getBottom()
                            + currentIndex
                            / (float) (numEntries - maxVisibleEntries)
                            * (buttonDownRect.getTop()
                                    - buttonUpRect.getBottom() - (buttonDownRect
                                    .getTop() - buttonUpRect.getBottom())
                                    / (float) (numEntries - maxVisibleEntries)),
                    WIDTH, (buttonDownRect.getTop() - buttonUpRect.getBottom())
                            / (float) (numEntries - maxVisibleEntries)));
            renderer.setColorRGB(1, 1, 1);
        }

        super.draw(renderer);
    }

    @Override
    public void onMouseDown(ScreenMouseEvent e) {
        if (buttonUpRect.translate(getAbsolutePosition()).containsPoint(
                e.getPos())) {
            if (currentIndex > 0) {
                if (currentWaitTime > SCROLL_DELAY) {
                    currentIndex--;
                    currentWaitTime = 0;
                }
            }
        }

        if (buttonDownRect.translate(getAbsolutePosition()).containsPoint(
                e.getPos())) {
            if (currentIndex >= 0
                    && currentIndex < numEntries - maxVisibleEntries) {
                if (currentWaitTime > SCROLL_DELAY) {
                    currentIndex++;
                    currentWaitTime = 0;
                }
            }
        }
    }

    @Override
    public void onMouseHover(ScreenMouseEvent e) {
    }

    @Override
    public void onMouseClicked(ScreenMouseEvent e) {
    }

}
