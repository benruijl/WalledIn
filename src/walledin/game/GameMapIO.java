/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin.game;

/**
 * Reads or writes a map from or to a certain format. The details of
 * the format are taken care of by the classes that implement this interface.
 * @author ben
 */
public interface GameMapIO {
    public GameMap readFromFile(String filename);
    public boolean writeToFile(GameMap map, String filename);
}
