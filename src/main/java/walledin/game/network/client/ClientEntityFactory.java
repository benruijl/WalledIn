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
package walledin.game.network.client;

import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.game.AbstractEntityFactory;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.logic.PlayerAnimationBehavior;
import walledin.game.entity.behaviors.logic.PlayerParentBehavior;
import walledin.game.entity.behaviors.logic.WeaponBehavior;
import walledin.game.entity.behaviors.physics.SpatialBehavior;
import walledin.game.entity.behaviors.render.BackgroundRenderBehavior;
import walledin.game.entity.behaviors.render.ItemRenderBehavior;
import walledin.game.entity.behaviors.render.MapRenderBehavior;
import walledin.game.entity.behaviors.render.PlayerRenderBehavior;
import walledin.game.entity.behaviors.render.WeaponRenderBehavior;
import walledin.util.Utils;
import walledin.util.XMLReader;

public class ClientEntityFactory extends AbstractEntityFactory {
	public ClientEntityFactory() {
		super();
		addStandardEntityCreationFunctions();
	}

	private Entity createPlayer(final Entity player) {
		player.setAttribute(Attribute.ORIENTATION, 1); // start looking to
		// the right

		player.addBehavior(new PlayerAnimationBehavior(player));
		player.addBehavior(new PlayerRenderBehavior(player));
		// spatial behavior does the interpolation in between server messages
		player.addBehavior(new SpatialBehavior(player));
		player.addBehavior(new PlayerParentBehavior(player));

		// FIXME correct the drawing instead of the hack the bounding box
		player.setAttribute(Attribute.BOUNDING_RECT,
				new Rectangle(0, 0, 44, 43));

		return player;
	}

	private Entity createBackground(final Entity ent) {
		ent.addBehavior(new BackgroundRenderBehavior(ent));
		return ent;
	}

	private Entity createGameMap(final Entity map) {
		map.addBehavior(new MapRenderBehavior(map));
		return map;
	}

	private Entity createBullet(final String texPart, final Rectangle destRect,
			final Element el, final Entity bl) {
		bl.addBehavior(new ItemRenderBehavior(bl, texPart, destRect));
		// spatial behavior does the interpolation in between server messages
		bl.addBehavior(new SpatialBehavior(bl));
		return bl;
	}
	
	private Entity createFoamPartical(final String texPart, final Rectangle destRect,
			final Element el, final Entity bl) {
		bl.addBehavior(new ItemRenderBehavior(bl, texPart, destRect));
		return bl;
	}

	private Entity createArmorKit(final String texPart,
			final Rectangle destRect, final Element el, final Entity ak) {
		ak.addBehavior(new ItemRenderBehavior(ak, texPart, destRect));
		return ak;
	}

	private Entity createHealthKit(final String texPart,
			final Rectangle destRect, final Element el, final Entity hk) {
		hk.addBehavior(new ItemRenderBehavior(hk, texPart, destRect));
		return hk;
	}
	
	private Entity createWeapon(final String texPart,
			final Rectangle destRect, final Element el, final Entity hg) {
		hg.addBehavior(new SpatialBehavior(hg));
		hg.addBehavior(new WeaponRenderBehavior(hg, texPart, destRect));
		return hg;
	}

	/**
	 * Creates a function that can create items of a particular family. It takes
	 * care of reading extra information, specific for the item, from the XML.
	 * 
	 * The creation of the item involves adding the required behaviors and
	 * setting its variables.
	 * 
	 * @param familyName
	 *            Name of the family. i.e healthkit, armourkit.
	 * @param texPart
	 *            Texture part
	 * @param destRect
	 *            Where to draw the texture part
	 * @param el
	 *            Element in XML file which contains item specific information,
	 *            like health kit strength or armor penetration value
	 */
	private void addItemFunction(final String familyName, final String texPart,
			final Rectangle destRect, final Element el) {

		if (familyName.equals("healthkit")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hk) {
							return createHealthKit(texPart, destRect, el, hk);
						}
					});
		}

		if (familyName.equals("armourkit")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity ak) {
							return createArmorKit(texPart, destRect, el, ak);
						}
					});
		}

		if (familyName.equals("handgunbullet")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createBullet(texPart, destRect, el, bl);

						}
					});
		}
		
		if (familyName.equals("foambullet")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createBullet(texPart, destRect, el, bl);

						}
					});
		}
		
		if (familyName.equals("foampartical")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createFoamPartical(texPart, destRect, el, bl);

						}
					});
		}
		
		if (familyName.equals("handgun")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hg) {
							return createWeapon(texPart, destRect, el, hg);
						}
					});
		}
		
		if (familyName.equals("foamweapon")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hg) {
							return createWeapon(texPart, destRect, el, hg);
						}
					});
		}
	}

	/**
	 * Creates skeletons for all the standard entities, like player and map.
	 */
	private void addStandardEntityCreationFunctions() {
		entityContructionFunctions.put("Player",
				new EntityConstructionFunction() {

					@Override
					public Entity create(final Entity ent) {
						return createPlayer(ent);
					}
				});

		entityContructionFunctions.put("Background",
				new EntityConstructionFunction() {

					@Override
					public Entity create(final Entity ent) {
						return createBackground(ent);
					}
				});

		entityContructionFunctions.put("Map", new EntityConstructionFunction() {

			@Override
			public Entity create(final Entity ent) {
				return createGameMap(ent);
			}
		});
	}

	/**
	 * @see walledin.game.EntityFactory#loadItemsFromXML(java.lang.String)
	 */
	@Override
	public boolean loadItemsFromXML(final URL filename) {
		final XMLReader reader = new XMLReader();

		if (reader.open(filename)) {
			final List<Element> elList = XMLReader.getElements(reader
					.getRootElement(), "item");

			final String texture = reader.getRootElement().getAttribute(
					"texture");
			final String texName = reader.getRootElement().getAttribute(
					"texname");
			URL textureURL = Utils.getClasspathURL(texture);
			TextureManager.getInstance().loadFromURL(textureURL, texName);

			for (final Element cur : elList) {
				final String familyName = XMLReader.getTextValue(cur, "name");
				final int destWidth = XMLReader.getIntValue(cur, "width");
				final int destHeight = XMLReader.getIntValue(cur, "height");

				final Element texurePart = XMLReader.getFirstElement(cur,
						"texpart");
				final String texPartName = XMLReader.getTextValue(texurePart,
						"name");
				final int x = XMLReader.getIntValue(texurePart, "x");
				final int y = XMLReader.getIntValue(texurePart, "y");
				final int width = XMLReader.getIntValue(texurePart, "width");
				final int height = XMLReader.getIntValue(texurePart, "height");

				TexturePartManager.getInstance().createTexturePart(texPartName,
						texName, new Rectangle(x, y, width, height));

				addItemFunction(familyName, texPartName, new Rectangle(0, 0,
						destWidth, destHeight), cur);
			}

		}
		return false;
	}
}
