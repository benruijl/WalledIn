package walledin.game;

import java.util.List;

import org.w3c.dom.Element;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.BulletBehavior;
import walledin.game.entity.behaviors.HealthBehavior;
import walledin.game.entity.behaviors.HealthKitBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;
import walledin.math.Rectangle;
import walledin.util.XMLReader;

public class ServerEntityFactory extends AbstractEntityFactory {
	public ServerEntityFactory() {
		super();
		addCreationFunctions();
	}

	private Entity createPlayer(final Entity player) {
		// TODO spatial is missing?
		player.setAttribute(Attribute.ORIENTATION, 1); // start looking to
		// the right

		player.addBehavior(new HealthBehavior(player, 100, 100));
		// player.addBehavior(new PlayerControlBehaviour(player));
		// TODO create control simulation

		// FIXME correct the drawing instead of the hack the bounding box
		player.setAttribute(Attribute.BOUNDING_RECT,
				new Rectangle(0, 0, 44, 43));

		return player;
	}

	private Entity createGameMap(final Entity map) {
		return map;
	}

	private Entity createBullet(final Rectangle destRect, final Element el,
			final Entity bl) {
		bl.addBehavior(new SpatialBehavior(bl));
		bl.setAttribute(Attribute.BOUNDING_RECT, destRect);

		bl.addBehavior(new BulletBehavior(bl));
		return bl;
	}

	private Entity createArmorKit(final Rectangle destRect, final Element el,
			final Entity ak) {
		ak.addBehavior(new SpatialBehavior(ak));

		ak.setAttribute(Attribute.BOUNDING_RECT, destRect);
		return ak;
	}

	private Entity createHealthKit(final Rectangle destRect, final Element el,
			final Entity hk) {
		hk.addBehavior(new SpatialBehavior(hk));

		hk.setAttribute(Attribute.BOUNDING_RECT, destRect);

		// read extra data
		final int hkStrength = XMLReader.getIntValue(el, "strength");
		hk.addBehavior(new HealthKitBehavior(hk, hkStrength));
		return hk;
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
	private void addFunction(final String familyName, final Rectangle destRect,
			final Element el) {

		if (familyName.equals("healthkit")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity hk) {
							return createHealthKit(destRect, el, hk);
						}
					});
		}

		if (familyName.equals("armourkit")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity ak) {
							return createArmorKit(destRect, el, ak);
						}
					});
		}

		if (familyName.equals("bullet")) {
			entityContructionFunctions.put(familyName,
					new EntityConstructionFunction() {

						@Override
						public Entity create(final Entity bl) {
							return createBullet(destRect, el, bl);

						}
					});
		}
	}

	private void addCreationFunctions() {
		entityContructionFunctions.put("Player",
				new EntityConstructionFunction() {

					@Override
					public Entity create(final Entity ent) {
						return createPlayer(ent);
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
	 * Loads all information for the prototypes from an XML file.
	 * 
	 * @param filename
	 *            XML file
	 * @return True on success, false on failure
	 */
	@Override
	public boolean loadItemsFromXML(final String filename) {
		final XMLReader reader = new XMLReader();

		if (reader.open(filename)) {
			final List<Element> elList = XMLReader.getElements(reader
					.getRootElement(), "item");

			for (final Element cur : elList) {
				final String familyName = XMLReader.getTextValue(cur, "name");
				final int destWidth = XMLReader.getIntValue(cur, "width");
				final int destHeight = XMLReader.getIntValue(cur, "height");

				addFunction(familyName, new Rectangle(0, 0, destWidth,
						destHeight), cur);
			}

		}
		return false;
	}
}
