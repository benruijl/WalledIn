package walledin.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import walledin.engine.TextureManager;
import walledin.engine.TexturePartManager;
import walledin.engine.math.Rectangle;
import walledin.util.XMLReader;

public class ItemFactory {
	Map<String, Item> items;
		
	public ItemFactory() {
		items = new HashMap<String, Item>();
	}

	/**
	 * This function creates a prototype item of a specific family and calls the 
	 * constructor of the derived class. It gives the constructor an 
	 * element in the XML file to read more information from.
	 * @param familyName Name of the family. i.e healthkit, armourkit.
	 * @param texPart Texture part
	 * @param el Element in XML file
	 * @return Returns the created item
	 */
	private Item create(String familyName, String texPart, Element el)
	{
		if (familyName.equals("healthkit"))
			return new HealthKitItem("PROT_HK", texPart, el);
		
		return null;
	}
	
	/** 
	 * Loads all information for the prototypes from an XML file.
	 * @param filename XML file
	 * @return True on success, false on failure
	 */
	public boolean loadFromXML(String filename)
	{
		XMLReader reader = new XMLReader();
		
		if (reader.open(filename))
		{
			List<Element> elList = XMLReader.getElements(reader.getRootElement(), "item");
			
			String texture = reader.getRootElement().getAttribute("texture");
			String texName = reader.getRootElement().getAttribute("texname");
			TextureManager.getInstance().loadFromFile(texture, texName); // load texture
			
			for (int i = 0; i < elList.size(); i++)
			{
				Element cur = elList.get(i);
				String familyName = XMLReader.getTextValue(cur, "name");
								
				List<Element> texEls = XMLReader.getElements(cur, "texpart");
				String texPartName = XMLReader.getTextValue(texEls.get(0), "name");
				final int x = XMLReader.getIntValue(texEls.get(0), "x");
				final int y = XMLReader.getIntValue(texEls.get(0), "y");
				final int width = XMLReader.getIntValue(texEls.get(0), "width");
				final int height = XMLReader.getIntValue(texEls.get(0), "height");
				
				TexturePartManager.getInstance().createTexturePart(texPartName, texName, new Rectangle(x, y, width, height));
				
				items.put(familyName, create(familyName, texPartName, cur));
			}
			
		}
		
		return false;
	}
	
	/**
	 * Creates a new item from a prototype of a given family.
	 * @param familyName Family name
	 * @param itemName The name of the item to be created
	 * @return Returns an item or null on failure
	 */
	public Item get(String familyName, String itemName)
	{
		if (!items.containsKey(familyName))
			throw new IllegalArgumentException("Item " + familyName
					+ " is not found in the database. Are the items loaded correctly?");
		
		try {
			Item it = (Item) items.get(familyName).clone(); // create a copy of the prototype
			it.setName(itemName); // change its name from the prototype to the new name
			return it;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
