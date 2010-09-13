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
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.Font;
import walledin.engine.Renderer;
import walledin.engine.gui.AbstractScreen;
import walledin.engine.gui.FontType;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class ListView extends AbstractScreen {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ListView.class);

    private final int numColumns;
    private String[] columns;
    private float[] columnWidth;
    private final List<String[]> data;

    public ListView(AbstractScreen parent, Rectangle boudingRect, int z,
            int numColumns, final String names[], float[] columnWidth) {
        super(parent, boudingRect, z);
        data = new ArrayList<String[]>();
        this.numColumns = numColumns;

        if (numColumns == names.length) {
            columns = names;
        }

        if (numColumns == columnWidth.length) {
            this.columnWidth = columnWidth;
        }
    }

    public void setColumns(final String names[]) {
        if (names.length == numColumns) {
            columns = names;
        }
    }

    public void setColumnWidth(float[] columnWidth) {
        if (columnWidth.length == numColumns) {
            this.columnWidth = columnWidth;
        }
    }

    public void addData(final String[] entry) {
        if (entry.length != numColumns) {
            LOG.error("Trying to add data to listview which does not have the same amount of columns.");
            return;
        }

        data.add(entry);
    }

    public void resetData() {
        data.clear();
    }

    protected List<String[]> getData() {
        return data;
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont(FontType.BUTTON_CAPTION);

        float curX = 10; // starting position

        for (int i = 0; i < numColumns; i++) {

            font.renderText(renderer, columns[i], new Vector2f(curX, 40));

            float curY = 60;

            for (String[] entry : data) {
                font.renderText(renderer, entry[i], new Vector2f(curX, curY));
                curY += 20;
            }

            curX += columnWidth[i];
        }

        renderer.drawRectOutline(getRectangle());
        super.draw(renderer);
    }
}
