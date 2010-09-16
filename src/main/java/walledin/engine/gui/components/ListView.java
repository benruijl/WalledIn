/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine.gui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.gui.ScreenMouseEvent;
import walledin.engine.gui.ScreenMouseEventListener;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class ListView<T> extends AbstractScreen implements
        ScreenMouseEventListener {

    public static class RowData<T> {
        private final T object;
        private final String[] data;

        public RowData(final T object, final String[] data) {
            this.object = object;
            this.data = data;
        }

        public T getObject() {
            return object;
        }

        public String[] getData() {
            return data;
        }
    }

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ListView.class);
    private static final float START_X_LIST = 10f;
    private static final float START_Y_CAPTION = 40f;
    private static final float START_Y_LIST = 60f;
    private static final float Y_SPACE = 20f;

    private final int maxVisible;
    private final int numColumns;
    private final Button[] columns;
    private float[] columnWidth;
    private final float[] accumulatedColumnWidth;
    private final List<RowData<T>> data;
    private Comparator<RowData<T>> lastComparator;
    private int selected;

    private final ScrollBar scrollBar;

    public ListView(final AbstractScreen parent, final Rectangle boudingRect,
            final int z, final int numColumns, final String names[],
            final float[] columnWidth, int maxVisible) {
        super(parent, boudingRect, z);

        data = new ArrayList<RowData<T>>();

        this.numColumns = numColumns;
        this.maxVisible = maxVisible;

        accumulatedColumnWidth = new float[numColumns];
        setColumnWidth(columnWidth);

        columns = new Button[numColumns];
        selected = -1;

        float curX = START_X_LIST;
        for (int i = 0; i < numColumns; i++) {
            columns[i] = new Button(this, names[i], new Vector2f(curX,
                    START_Y_CAPTION));
            curX += columnWidth[i];
            columns[i].addMouseEventListener(this);
            addChild(columns[i]);
        }

        scrollBar = new ScrollBar(this, 2, maxVisible);
        addChild(scrollBar);
        scrollBar.show();

        lastComparator = getComparator(0);
        addMouseEventListener(this);
    }

    public void setColumnWidth(final float[] columnWidth) {
        if (columnWidth.length == numColumns) {
            this.columnWidth = columnWidth;

            accumulatedColumnWidth[0] = columnWidth[0];
            for (int i = 1; i < numColumns; i++) {
                accumulatedColumnWidth[i] += accumulatedColumnWidth[i - 1]
                        + columnWidth[i];
            }
        }
    }

    public void addData(final RowData<T> entry) {
        if (entry.getData().length != numColumns) {
            LOG.error("Trying to add data to listview which does not have the same amount of columns.");
            return;
        }

        data.add(entry);
        scrollBar.setNumEntries(data.size());
    }

    /**
     * Sort data according to the last used comparator.
     */
    public final void sortData() {
        Collections.sort(data, lastComparator);
    }

    public void resetData() {
        data.clear();
        scrollBar.setNumEntries(0);
    }

    protected List<RowData<T>> getData() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);

        if (selected >= 0 && selected < data.size()) {
            renderer.setColorRGBA(0.2f, 0.2f, 0.02f, 0.5f);
            renderer.drawFilledRect(new Rectangle(START_X_LIST, START_Y_LIST
                    + (selected - 1) * Y_SPACE + 2,
                    accumulatedColumnWidth[numColumns - 1], Y_SPACE + 2));
            renderer.setColorRGBA(1, 1, 1, 1);
        }

        float curX = START_X_LIST;

        for (int i = 0; i < numColumns; i++) {
            float curY = START_Y_LIST;

            for (int j = 0; j < data.size(); j++) {
                font.renderText(renderer, data.get(j).getData()[i],
                        new Vector2f(curX, curY));
                curY += Y_SPACE;
            }

            curX += columnWidth[i];
        }

        renderer.drawRectOutline(getRectangle());
        super.draw(renderer);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
        for (int i = 0; i < data.size(); i++) {
            /* The i -1 is because the text is rendered above the line. */
            if (new Rectangle(START_X_LIST, START_Y_LIST + (i - 1) * Y_SPACE,
                    accumulatedColumnWidth[numColumns - 1], Y_SPACE).translate(
                    getAbsolutePosition()).containsPoint(e.getPos())) {
                selected = i;
                return;
            }
        }

        selected = -1;
    }

    protected Comparator<RowData<T>> getComparator(final int column) {
        return new Comparator<RowData<T>>() {
            @Override
            public int compare(final RowData<T> o1, final RowData<T> o2) {
                return o1.getData()[column].compareTo(o2.getData()[column]);
            }
        };
    }

    protected void onListItemClicked(final T item) {
    }

    @Override
    public void onMouseClicked(final ScreenMouseEvent e) {
        for (int i = 0; i < numColumns; i++) {
            if (e.getScreen() == columns[i]) {
                final Comparator<RowData<T>> comp = getComparator(i);
                Collections.sort(data, comp);
                lastComparator = comp;
                return;
            }
        }

        for (int i = 0; i < data.size(); i++) {
            /* The i -1 is because the text is rendered above the line. */
            if (new Rectangle(START_X_LIST, START_Y_LIST + (i - 1) * Y_SPACE,
                    accumulatedColumnWidth[numColumns - 1], Y_SPACE).translate(
                    getAbsolutePosition()).containsPoint(e.getPos())) {
                onListItemClicked(data.get(i).getObject());
                return;
            }
        }

    }
}
