package walledin.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.game.entity.behaviors.HealthKitBehavior;
import walledin.util.XMLReader;

/**
 * Singleton class for creating items
 */
public class ItemFactory {
	private static final ItemFactory INSTANCE = new ItemFactory();

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static ItemFactory getInstance() {
		return INSTANCE;
	}

	private ItemFactory() {
		itemContructionFunctions = new HashMap<String, ItemConstructionFunction>();
	}

	private interface ItemConstructionFunction {
		Item create(String itemName);
	}

	Map<String, ItemConstructionFunction> itemContructionFunctions;

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
	 * @param scale
	 *            The scale with which to draw
	 * @param el
	 *            Element in XML file which contains item specific information,
	 *            like health kit strength or armor penetration value
	 */
	private void addFunction(final String familyName, final String texPart,
			final Rectangle destRect, final Element el) {
		if (familyName.equals("healthkit")) {
			itemContructionFunctions.put("healthkit",
					new ItemConstructionFunction() {

						@Override
						public Item create(final String itemName) {
							final Item hk = new Item(itemName, texPart,
									destRect);

							// read extra data
							final int hkStrength = XMLReader.getIntValue(el,
									"strength");
							hk
									.addBehavior(new HealthKitBehavior(hk,
											hkStrength));
							return hk;
						}
					});
		}

		if (familyName.equals("armourkit")) {
			itemContructionFunctions.put("armourkit",
					new ItemConstructionFunction() {

						@Override
						public Item create(final String itemName) {
							return new Item(itemName, texPart, destRect); // TODO:
							// read
							// custom
							// information
						}
					});
		}
	}

	/**
	 * Loads all information for the prototypes from an XML file.
	 * 
	 * @param filename
	 *            XML file
	 * @return True on success, false on failure
	 */
	public boolean loadFromXML(final String filename) {
		final XMLReader reader = new XMLReader();

		if (reader.open(filename)) {
			final List<Element> elList = XMLReader.getElements(reader
					.getRootElement(), "item");

			final String texture = reader.getRootElement().getAttribute(
					"texture");
			final String texName = reader.getRootElement().getAttribute(
					"texname");
			TextureManager.getInstance().loadFromFile(texture, texName);

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

				addFunction(familyName, texPartName, new Rectangle(0, 0,
						destWidth, destHeight), cur);
			}

		}

		return false;
	}

	/**
	 * Creates a new item of a given family.
	 * 
	 * @param familyName
	 *            Family name
	 * @param itemName
	 *            The name of the item to be created
	 * @return Returns an item or null on failure
	 */
	public Item create(final String familyName, final String itemName) {
		if (!itemContructionFunctions.containsKey(familyName)) {
			throw new IllegalArgumentException(
					"Item "
							+ familyName
							+ " is not found in the database. Are the items loaded correctly?");
		}

		return itemContructionFunctions.get(familyName).create(itemName);
	}
}
