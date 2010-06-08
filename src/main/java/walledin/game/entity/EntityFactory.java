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
package walledin.game.entity;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import walledin.game.EntityManager;

/**
 * Loads entity creation functions from the scripts and executes them when
 * created
 * 
 * @author Wouter Smeenk
 */
public class EntityFactory {
    private static final Logger LOG = Logger.getLogger(EntityFactory.class);
    private final List<Map<Family, EntityFunction>> maps;
    private final GroovyShell shell;

    public EntityFactory() {
        maps = new ArrayList<Map<Family, EntityFunction>>();
        shell = new GroovyShell(ClassLoader.getSystemClassLoader());
    }

    /**
     * Creates a entity. The loaded scripts will be applyed in the order they
     * where loaded. The creation function of the family will be looked up for
     * each script. All the functions of the parents up to the root will be
     * executed.
     * 
     * @param entityManager
     *            The entity manager
     * @param family
     *            The entity family to be created
     * @param entityName
     *            the name of the entity to be created
     * @return The created entity
     */
    public Entity create(final EntityManager entityManager,
            final Family family, final String entityName) {
        final Entity entity = new Entity(entityManager, family, entityName);
        boolean empty = true;
        for (Map<Family, EntityFunction> map : maps) {
            boolean result = callFunctions(family, entity, map);
            empty = empty && result;
        }
        if (empty) {
            LOG.warn("Entity " + entityName
                    + " created empty. No behaviors were added");
        }
        return entity;
    }

    /**
     * Calls the function corresponding to the family and calls this function
     * again on it's parent.
     * 
     * @param family
     *            The family to call the function for
     * @param entity
     *            the entity to call the function on
     * @param map
     *            contains the functions
     * @return If any functions have been called
     */
    private boolean callFunctions(final Family family, final Entity entity,
            final Map<Family, EntityFunction> map) {
        if (family == null) {
            return false;
        }
        // Call parent functions
        boolean result = callFunctions(family.getParent(), entity, map);
        final EntityFunction function = map.get(family);
        if (function != null) {
            function.create(entity);
            result = false;
        }
        return result;
    }

    /**
     * Loads and executes a script. It has to result in a map of type
     * Map<Family, EntityFunction>. This map will then be placed in a list and
     * will be applied to the created entity in the order the scripts are
     * loaded.
     * 
     * @param scriptURL
     *            the url of the script
     * @throws IOException
     *             If the script cannot be loaded
     */
    public void loadScript(final URL scriptURL) throws IOException {
        LOG.debug("Load script: " + scriptURL);
        final Reader reader = new InputStreamReader(scriptURL.openStream());
        LOG.debug("Opened reader");
        final Script script = shell.parse(reader);
        LOG.debug("Script parsed");
        @SuppressWarnings("unchecked")
        final Map<Family, EntityFunction> value = (Map<Family, EntityFunction>) script
                .run();
        LOG.debug("Load of script finished");

        maps.add(value);
    }
}
