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
    private Set<PlayerActions> playerActions;

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
