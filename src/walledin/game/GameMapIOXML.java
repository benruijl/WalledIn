/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import walledin.engine.TextureManager;

/**
 * Loads a map from an XML file
 * @author ben
 */
public class GameMapIOXML implements GameMapIO {
    private static Document dom;

    public GameMapIOXML() {
    }

    private void parseXmlFile(String filename) {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse(filename);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private GameMap parseDocument() {
        //get the root element
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("Map");
        if (nl != null && nl.getLength() > 0) {
            // map should only appear once
            Element el = (Element) nl.item(0);

            // read the map data
            GameMap m = readData(el);
            return m;
        }
        else
            System.err.print("Error loading map.");

        return null;
    }

    private GameMap readData(Element mapElement) {
        String name = getTextValue(mapElement, "Name");
        String tex = getTextValue(mapElement, "Texture");
        int width = getIntValue(mapElement, "Width");
        int height = getIntValue(mapElement, "Height");
        Tile[] tiles = parseTiles(mapElement, "Tiles");

        String texRes = TextureManager.getInstance().LoadFromFile(tex);

        GameMap m = new GameMap(name, texRes, width, height, tiles);

        return m;
    }

    private Tile[] parseTiles(Element ele, String tagName) {
        String tiles = getTextValue(ele, tagName);

        StringTokenizer st = new StringTokenizer(tiles);
        ArrayList<Tile> til = new ArrayList<Tile>();


        while (st.hasMoreTokens()) {
            String s = st.nextToken();

            til.add(new Tile(Integer.parseInt(s)));
        }

        return til.toArray(new Tile[0]);
    }

    /**
     * I take a xml element and the tag name, look for the tag and get
     * the text content
     * i.e for <employee><name>John</name></employee> xml snippet if
     * the Element points to employee node and tagName is 'name' I will return John
     */
    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
        try
        {
        return Integer.parseInt(getTextValue(ele, tagName));
        } catch (NumberFormatException nu) {

            System.err.print("Error converting to int in tag " + tagName + ": " + nu.getMessage());
        }

        return 0;
    }

    /**
     * Reads map data from an XML file
     * @param filename Filename of XML data
     * @return Returns true on success and false on failure
     */
    public GameMap readFromFile(String filename) {
        parseXmlFile(filename);
        return parseDocument();
    }

    public boolean writeToFile(GameMap map, String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
