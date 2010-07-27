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
package walledin.game.gui;

import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.TextureManager;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.gui.ScreenManager.ScreenType;
import walledin.game.gui.components.Button;
import walledin.util.Utils;

public class SelectTeamScreen extends Screen implements
        ScreenMouseEventListener {
    Screen teamBlue;
    Screen teamRed;
    Screen back;

    public SelectTeamScreen(final ScreenManager manager) {
        super(manager, null, 0);
    }

    @Override
    public void draw(final Renderer renderer) {
        super.draw(renderer);

        renderer.drawRect("logo", new Rectangle(250, 50, 256, 128));
    }

    @Override
    public void initialize() {
        TextureManager.getInstance().loadFromURL(
                Utils.getClasspathURL("logo.png"), "logo");

        teamBlue = new Button(this, "Team Blue", new Vector2f(200, 200));
        teamBlue.addMouseEventListener(this);
        teamRed = new Button(this, "Team Red", new Vector2f(400, 200));
        teamRed.addMouseEventListener(this);
        back = new Button(this, "Back", new Vector2f(200, 400));
        back.addMouseEventListener(this);
        addChild(teamBlue);
        addChild(teamRed);
        addChild(back);
    }

    @Override
    public void onMouseDown(final ScreenMouseEvent e) {
        if (e.getScreen() == teamBlue) {
            getManager().getScreen(ScreenType.GAME).initialize();
            getManager().getScreen(ScreenType.GAME).show();
            hide();
        }

        if (e.getScreen() == teamRed) {
            getManager().getScreen(ScreenType.GAME).initialize();
            getManager().getScreen(ScreenType.GAME).show();
            hide();
        }
        if (e.getScreen() == back) {
            getManager().getScreen(ScreenType.SERVER_LIST).show();
            hide();
            Input.getInstance().setButtonUp(1); // FIXME
        }
    }

    @Override
    public void onMouseHover(final ScreenMouseEvent e) {
    }

}
