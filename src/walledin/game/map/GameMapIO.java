package walledin.game.map;

import walledin.game.entity.Entity;

/**
 * Reads or writes a map from or to a certain format. The details of the format
 * are taken care of by the classes that implement this interface.
 * 
 * @author ben
 */
public interface GameMapIO {
	Entity readFromFile(final String filename);

	boolean writeToFile(final Entity map, final String filename);
}
