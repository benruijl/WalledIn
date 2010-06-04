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
package walledin.game.network.server;

import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

import walledin.engine.math.Rectangle;
import walledin.game.AbstractEntityFactory;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.behaviors.PhysicsBehavior;
import walledin.game.entity.behaviors.logic.BulletBehavior;
import walledin.game.entity.behaviors.logic.FoamBulletBehavior;
import walledin.game.entity.behaviors.logic.HealthBehavior;
import walledin.game.entity.behaviors.logic.HealthKitBehavior;
import walledin.game.entity.behaviors.logic.PlayerParentBehavior;
import walledin.game.entity.behaviors.logic.PlayerWeaponInventoryBehavior;
import walledin.game.entity.behaviors.logic.WeaponBehavior;
import walledin.game.entity.behaviors.physics.PlayerControlBehaviour;
import walledin.game.entity.behaviors.physics.SpatialBehavior;
import walledin.util.XMLReader;

public class ServerEntityFactory extends AbstractEntityFactory {
	public ServerEntityFactory() {
		super();
		addStandardEntityCreationFunctions();
	}

	private Entity createPlayer(final Entity player) {
		player.setAttribute(Attribute.ORIENTATION, 1); // start looking to
		// the right

		player.addBehavior(new HealthBehavior(player, 100, 100));
		player.addBehavior(new PlayerControlBehaviour(player));
		player.addBehavior(new PlayerParentBehavior(player));
		player.addBehavior(new PhysicsBehavior(player));
		player.addBehavior(new PlayerWeaponInventoryBehavior(player));

		// FIXME correct the drawing instead of the hack the bounding box
		player.setAttribute(Attribute.BOUNDING_RECT,
				new Rectangle(0, 0, 44, 43));
		// player.setAttribute(Attribute.BOUNDING_CIRCLE, new Circle());

		return player;
	}

	private Entity createGameMap(final Entity map) {
		map.setAttribute(Attribute.RENDER_TILE_SIZE, 32f);
		return map;
	}

	private Entity createHandgunBullet(final Rectangle destRect, final Element el,
			final Entity bl) {
		bl.addBehavior(new SpatialBehavior(bl));
		bl.addBehavior(new PhysicsBehavior(bl, false , false));
		bl.setAttribute(Attribute.BOUNDING_RECT, destRect);

		bl.addBehavior(new BulletBehavior(bl));
		return bl;
	}
	
	private Entity createFoamBullet(final Rectangle destRect, final Element el,
			final Entity bl) {
		bl.addBehavior(new SpatialBehavior(bl));
		bl.addBehavior(new PhysicsBehavior(bl, false , false));
		bl.setAttribute(Attribute.BOUNDING_RECT, destRect);

		bl.addBehavior(new FoamBulletBehavior(bl));
		return bl;
	}
	
	private Entity createFoamPartical(final Rectangle destRect, final Element el,
			final Entity bl) {
		bl.addBehavior(new SpatialBehavior(bl));
		bl.setAttribute(Attribute.BOUNDING_RECT, destRect);
		return bl;
	}

	private Entity createArmorKit(final Rectangle destRect, final Element el,
			final Entity ak) {
		ak.addBehavior(new SpatialBehavior(ak));
		ak.addBehavior(new PhysicsBehavior(ak));
		
		ak.setAttribute(Attribute.BOUNDING_RECT, destRect);
		return ak;
	}

	private Entity createHealthKit(final Rectangle destRect, final Element el,
			final Entity hk) {
		hk.addBehavior(new SpatialBehavior(hk));
		hk.addBehavior(new PhysicsBehavior(hk));
		hk.setAttribute(Attribute.BOUNDING_RECT, destRect);

		// read extra data
		final int hkStrength = XMLReader.getIntValue(el, "strength");
		hk.addBehavior(new HealthKitBehavior(hk, hkStrength));
		return hk;
	}
	
	private Entity createWeapon(final Rectangle destRect, final Entity hg, final Family bulletFamily) {
		hg.addBehavior(new SpatialBehavior(hg));
		// hg.addBehavior(new PhysicsBehavior(hg));
		hg.setAttribute(Attribute.BOUNDING_RECT, destRect);
		hg.addBehavior(new WeaponBehavior(hg, 10, bulletFamily));
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
	private void addItemFunction(final Family familyName,
			final Rectangle destRect, final Element el) {

		if (familyName.equals(Family.HEALTHKIT)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hk) {
							return createHealthKit(destRect, el, hk);
						}
					});
		}

		if (familyName.equals(Family.ARMOURKIT)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity ak) {
							return createArmorKit(destRect, el, ak);
						}
					});
		}

		if (familyName.equals(Family.HANDGUN_BULLET)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createHandgunBullet(destRect, el, bl);

						}
					});
		}
		
		if (familyName.equals(Family.FOAMGUN_BULLET)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createFoamBullet(destRect, el, bl);

						}
					});
		}
		
		if (familyName.equals(Family.FOAM_PARTICLE)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createFoamPartical(destRect, el, bl);

						}
					});
		}


		if (familyName.equals(Family.HANDGUN)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hg) {
							return createWeapon(destRect, hg, Family.HANDGUN_BULLET);
						}
					});
		}
		
		if (familyName.equals(Family.FOAMGUN)) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hg) {
							return createWeapon(destRect, hg, Family.FOAMGUN_BULLET);
						}
					});
		}
	}

	/**
	 * Creates skeletons for all the standard entities, like player and map.
	 */
	private void addStandardEntityCreationFunctions() {
		entityContructionFunctions.put(Family.PLAYER,
				new EntityConstructionFunction() {

					@Override
					public Entity create(final Entity ent) {
						return createPlayer(ent);
					}
				});

		entityContructionFunctions.put(Family.MAP, new EntityConstructionFunction() {

			@Override
			public Entity create(final Entity ent) {
				return createGameMap(ent);
			}
		});

	}

	/**
	 * Loads all information for the prototypes from an XML file.
	 * 
	 * @param file
	 *            XML file
	 * @return True on success, false on failure
	 */
	@Override
	public boolean loadItemsFromXML(final URL file) {
		final XMLReader reader = new XMLReader();

		if (reader.open(file)) {
			final List<Element> elList = XMLReader.getElements(reader
					.getRootElement(), "item");

			for (final Element cur : elList) {
				final String familyName = XMLReader.getTextValue(cur, "name");
				final int destWidth = XMLReader.getIntValue(cur, "width");
				final int destHeight = XMLReader.getIntValue(cur, "height");

				addItemFunction(Enum.valueOf(Family.class, familyName), new Rectangle(0, 0, destWidth,
						destHeight), cur);
			}

		}
		return false;
	}
}
