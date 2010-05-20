package walledin.game.map;

import walledin.game.entity.Entity;

/**
 * Reads or writes a map from or to a certain format. The details of the format
 * are taken care of by the classes that implement this interface.
 * 
 * @author ben
 */
public interface GameMapIO {
	Entity readFromFile(String filename);

	boolean writeToFile(Entity map, String filename);
}
