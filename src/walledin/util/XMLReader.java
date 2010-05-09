package walledin.util;

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
import walledin.game.GameMap;
import walledin.game.Tile;

public class XMLReader {
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
}
