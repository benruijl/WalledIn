package walledin.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {
	Document dom;
	Element root;

	public boolean open(final String filename) {
		// get the factory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			final DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(filename);
			root = dom.getDocumentElement();
			return true;

		} catch (final ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (final SAXException se) {
			se.printStackTrace();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		return false;
	}

	public Element getRootElement() {
		return root;
	}

	/**
	 * Gets all the child elements with name <i>tag</i> of the element el.
	 * 
	 * @param el
	 *            Current element
	 * @param tag
	 *            Tag name
	 * @return List of sub-elements with name tag or null on failure
	 */
	public List<Element> getElements(final Element el, final String tag) {
		final NodeList nl = el.getElementsByTagName(tag);
		if (nl == null) {
			return null;
		}

		final List<Element> childElements = new ArrayList<Element>();

		for (int i = 0; i < nl.getLength(); i++) {
			childElements.add((Element) nl.item(i));
		}

		return childElements;
	}

	/**
	 * Returns the text value of a tag in the given element. Example:</br> XML
	 * file:<book><name>ABC</name></book> This function will return ABC.
	 * 
	 * @param el
	 *            Current element
	 * @param tag
	 *            Tag name
	 * @return Text of the value of tag node, or null on failure
	 */
	public String getTextValue(final Element el, final String tag) {
		String textVal = null;
		final NodeList nl = el.getElementsByTagName(tag);

		if (nl != null && nl.getLength() > 0) {
			final Element childEl = (Element) nl.item(0);
			textVal = childEl.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	/**
	 * Calls getTextValue and returns a integer value
	 * 
	 * @param ele
	 *            Current element
	 * @param tag
	 *            Tag name
	 * @return Integer value in tag, or null on failure
	 */
	public Integer getIntValue(final Element el, final String tag) {
		try {
			return Integer.parseInt(getTextValue(el, tag));
		} catch (final NumberFormatException nu) {

			System.err.print("Error converting to int in tag " + tag + ": "
					+ nu.getMessage());
		}

		return null;
	}

	public Element getFirstElement(final Element element, final String tag) {
		final List<Element> elementList = getElements(element, tag);
		// assumes there is only one map
		if (elementList != null && !elementList.isEmpty()) {
			return elementList.get(0);
		} else {
			return null;
		}
	}
}
