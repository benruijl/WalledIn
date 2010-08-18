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

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.util.SettingsManager;

/** This class contains all information about the player. */
public final class PlayerInfo extends PlayerClientInfo {
    private final static float RESPAWN_TIME = SettingsManager.getInstance()
            .getFloat("game.respawnTime");
    private final Entity player;
    private float currentRespawnTime;
    private boolean dead;
    private boolean respawn;
    private float walledInTime;

    public PlayerInfo(final Entity player) {
        super(player.getName(), Team.UNSELECTED);
        this.player = player;
        dead = false;
        respawn = false;
    }

    public Entity getPlayer() {
        return player;
    }

    public boolean isDead() {
        return dead;
    }

    public void setWalledInTime(final float walledInTime) {
        this.walledInTime = walledInTime;
    }

    public float getWalledInTime() {
        return walledInTime;
    }

    /**
     * Should be called when the player has been respawned.
     */
    public void hasRespawned() {
        dead = false;
        respawn = false;
    }

    /**
     * Checks if the player died and determines if the player should be
     * respawned.
     * 
     * @param delta
     *            Delta time since last update
     */
    public void update(final double delta) {
        /* Check if the player died */
        if (!dead && (Integer) player.getAttribute(Attribute.HEALTH) == 0) {
            dead = true;
            respawn = false;
            player.remove(); // remove the player
        }

        if (dead && !respawn) {
            currentRespawnTime += delta;

            if (currentRespawnTime > RESPAWN_TIME) {
                currentRespawnTime = 0;
                respawn = true;
            }
        }
    }

    public boolean shouldRespawn() {
        return respawn;
    }
}