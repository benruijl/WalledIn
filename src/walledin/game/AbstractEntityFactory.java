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

import java.util.HashMap;
import java.util.Map;

import walledin.game.entity.Entity;

public abstract class AbstractEntityFactory implements EntityFactory {
	public interface EntityConstructionFunction {
		Entity create(final Entity ent);
	}

	protected final Map<String, EntityConstructionFunction> entityContructionFunctions;

	public AbstractEntityFactory() {
		entityContructionFunctions = new HashMap<String, EntityConstructionFunction>();
	}

	/**
	 * @see walledin.game.EntityFactory#create(java.lang.String,
	 *      java.lang.String)
	 */
	public Entity create(final String familyName, final String entityName) {
		final Entity ent = new Entity(familyName, entityName);
		final EntityConstructionFunction func = entityContructionFunctions
				.get(familyName);

		if (func == null) {
			return ent; // return generic entity
		}

		return func.create(ent);
	}

	@Override
	public abstract boolean loadItemsFromXML(String filename);

}