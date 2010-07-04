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
package walledin.game;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.Input;

public class PlayerActionManager {
    private static final Logger LOG = Logger
            .getLogger(PlayerActionManager.class);
    private static PlayerActionManager ref = null;
    private final Map<Integer, PlayerActions> keyMap;
    private final Map<Integer, PlayerActions> buttonMap;
    private final Set<PlayerActions> playerActions;

    private PlayerActionManager() {
        keyMap = new HashMap<Integer, PlayerActions>();
        buttonMap = new HashMap<Integer, PlayerActions>();
        playerActions = new HashSet<PlayerActions>();

        /* Add standard mapping */
        keyMap.put(KeyEvent.VK_A, PlayerActions.WALK_LEFT);
        keyMap.put(KeyEvent.VK_D, PlayerActions.WALK_RIGHT);
        keyMap.put(KeyEvent.VK_SPACE, PlayerActions.JUMP);
        keyMap.put(KeyEvent.VK_1, PlayerActions.SELECT_WEAPON_1);
        keyMap.put(KeyEvent.VK_2, PlayerActions.SELECT_WEAPON_2);
        buttonMap.put(MouseEvent.BUTTON1, PlayerActions.SHOOT_PRIMARY);
        buttonMap.put(MouseEvent.BUTTON2, PlayerActions.SHOOT_SECUNDARY);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static PlayerActionManager getInstance() {
        if (ref == null) {
            ref = new PlayerActionManager();
        }

        return ref;
    }

    public void update() {
        final Set<Integer> keysDown = Input.getInstance().getKeysDown();
        final Set<Integer> buttonsDown = Input.getInstance().getButtonsDown();

        playerActions.clear();

        for (final Integer key : keysDown) {
            if (keyMap.containsKey(key)) {
                playerActions.add(keyMap.get(key));
            }
        }

        for (final Integer button : buttonsDown) {
            if (buttonMap.containsKey(button)) {
                playerActions.add(buttonMap.get(button));
            }
        }
    }

    public Set<PlayerActions> getPlayerActions() {
        return playerActions;
    }
}
