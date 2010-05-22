package walledin.game;

import walledin.game.entity.Entity;

public interface EntityFactory {

	public abstract Entity create(final String familyName,
			final String entityName);

	/**
	 * Loads all information for the prototypes from an XML file.
	 * 
	 * @param filename
	 *            XML file
	 * @return True on success, false on failure
	 */
	public abstract boolean loadItemsFromXML(final String filename);

}