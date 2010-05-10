package walledin.game.map;

/**
 * Reads or writes a map from or to a certain format. The details of the format
 * are taken care of by the classes that implement this interface.
 * 
 * @author ben
 */
public interface GameMapIO {
	GameMap readFromFile(String filename);

	boolean writeToFile(GameMap map, String filename);
}
