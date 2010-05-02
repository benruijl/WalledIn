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
 * 
 * @author ben
 */
public class GameMapIOXML implements GameMapIO {
	private Document parseXmlFile(final String filename) {
		// get the factory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			final DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			return db.parse(filename);

		} catch (final ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (final SAXException se) {
			se.printStackTrace();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	private GameMap parseDocument(Document dom) {
		// get the root element
		final Element docEle = dom.getDocumentElement();

		final NodeList nl = docEle.getElementsByTagName("Map");
		if (nl != null && nl.getLength() > 0) {
			// map should only appear once
			final Element el = (Element) nl.item(0);

			// read the map data
			final GameMap m = readData(el);
			return m;
		} else {
			System.err.print("Error loading map.");
		}

		return null;
	}

	private GameMap readData(final Element mapElement) {
		final String name = getTextValue(mapElement, "Name");
		final String tex = getTextValue(mapElement, "Texture");
		final int width = getIntValue(mapElement, "Width");
		final int height = getIntValue(mapElement, "Height");
		final Tile[] tiles = parseTiles(mapElement, "Tiles");

		final String texRes = TextureManager.getInstance().LoadFromFile(tex);

		final GameMap m = new GameMap(name, texRes, width, height, tiles);

		return m;
	}

	private Tile[] parseTiles(final Element ele, final String tagName) {
		final String tiles = getTextValue(ele, tagName);

		final StringTokenizer st = new StringTokenizer(tiles);
		final ArrayList<Tile> til = new ArrayList<Tile>();

		while (st.hasMoreTokens()) {
			final String s = st.nextToken();

			til.add(new Tile(Integer.parseInt(s)));
		}

		return til.toArray(new Tile[0]);
	}

	/**
	 * I take a xml element and the tag name, look for the tag and get the text
	 * content i.e for <employee><name>John</name></employee> xml snippet if the
	 * Element points to employee node and tagName is 'name' I will return John
	 */
	private String getTextValue(final Element ele, final String tagName) {
		String textVal = null;
		final NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			final Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(final Element ele, final String tagName) {
		// in production application you would catch the exception
		try {
			return Integer.parseInt(getTextValue(ele, tagName));
		} catch (final NumberFormatException nu) {

			System.err.print("Error converting to int in tag " + tagName + ": "
					+ nu.getMessage());
		}

		return 0;
	}

	/**
	 * Reads map data from an XML file
	 * 
	 * @param filename
	 *            Filename of XML data
	 * @return Returns true on success and false on failure
	 */
	@Override
	public GameMap readFromFile(final String filename) {
		Document dom = parseXmlFile(filename);
		return parseDocument(dom);
	}

	@Override
	public boolean writeToFile(final GameMap map, final String filename) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
