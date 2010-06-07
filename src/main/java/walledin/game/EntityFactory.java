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

import groovy.lang.GroovyShell;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import walledin.game.entity.Entity;
import walledin.game.entity.Family;

public class EntityFactory {
    public List<Map<Family, EntityFunction>> maps;
    
    public EntityFactory() {
        maps = new ArrayList<Map<Family, EntityFunction>>();
    }
    
    public Entity create(final EntityManager entityManager,
            final Family family, final String entityName) {
        final Entity entity = new Entity(entityManager, family, entityName);
        for (Map<Family, EntityFunction> map : maps) {
            map.get(family).create(entity);
        }
        return entity;
    }

    public void loadScript(final URL scriptURL)
            throws CompilationFailedException, IOException {
        final GroovyShell shell = new GroovyShell();
        final Map<Family, EntityFunction> value = (Map<Family, EntityFunction>) shell
                .evaluate(scriptURL.openStream());

        maps.add(value);
    }
}
