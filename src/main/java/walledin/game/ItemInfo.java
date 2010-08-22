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

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;

public class ItemInfo {
    private static final Logger LOG = Logger.getLogger(ItemInfo.class);

    Family family;
    Vector2f position;
    float respawnTime;
    float waitTime;
    Entity associatedEntity;
    boolean removed;

    public ItemInfo(final Family family, final Vector2f position,
            final float respawnTime) {
        super();
        this.family = family;
        this.position = position;
        this.respawnTime = respawnTime;

        removed = false;
        waitTime = respawnTime;
        associatedEntity = null;
    }

    public Family getFamily() {
        return family;
    }

    public Vector2f getPosition() {
        return position;
    }

    public float getRespawnTime() {
        return respawnTime;
    }

    public void update(final double delta, final EntityManager entityManager) {
        /* Check if item should respawn */
        if (respawnTime < 0) {
            return;
        }

        /* Check if removed */
        removed = associatedEntity == null
                || Boolean.TRUE.equals(associatedEntity
                        .getAttribute(Attribute.PICKED_UP));

        if (removed) {
            waitTime += delta;

            if (waitTime >= respawnTime) {
                associatedEntity = entityManager.create(family);

                LOG.info("Respawned item of family " + family);

                associatedEntity.setAttribute(Attribute.POSITION, position);
                associatedEntity.setAttribute(Attribute.PICKED_UP,
                        Boolean.FALSE);

                removed = false;
                waitTime = 0.0f;
            }
        }
    }

    public boolean isRemoved() {
        return removed;
    }
}