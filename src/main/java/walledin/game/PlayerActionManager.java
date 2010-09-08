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
import walledin.util.SettingsManager;

final public class PlayerActionManager {
    private static final Logger LOG = Logger
            .getLogger(PlayerActionManager.class);
    private static PlayerActionManager ref = null;
    private final Map<Integer, PlayerAction> keyMap;
    private final Map<Integer, PlayerAction> buttonMap;
    private final Set<PlayerAction> playerActions;

    private PlayerActionManager() {
        keyMap = new HashMap<Integer, PlayerAction>();
        buttonMap = new HashMap<Integer, PlayerAction>();
        playerActions = new HashSet<PlayerAction>();

        /* Load the mapping from the configuration file */
        for (final PlayerAction action : PlayerAction.values()) {
            final String inputName = SettingsManager.getInstance().getString(
                    action.toString());

            if (inputName == null) {
                LOG.warn("Warning: action " + action + " is unassigned.");
                continue;
            }

            try {
                final int key = KeyEvent.class.getField(inputName).getInt(
                        KeyEvent.class);
                keyMap.put(key, action);
            } catch (final NoSuchFieldException e) {
                /* Not in key list, check the mouse button list */
                try {
                    final int button = MouseEvent.class.getField(inputName)
                            .getInt(KeyEvent.class);
                    buttonMap.put(button, action);
                } catch (final NoSuchFieldException e1) {
                    /* If not in any of the lists, print a warning */
                    LOG.warn("Warning: action " + action + " is unassigned.");
                } catch (final Exception e1) {
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
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

    /**
     * Clears the player action list.
     */
    public void clear() {
        playerActions.clear();
    }

    public Set<PlayerAction> getPlayerActions() {
        return playerActions;
    }
}
