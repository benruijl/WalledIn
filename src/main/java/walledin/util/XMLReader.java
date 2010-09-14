/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.util;

import java.io.IOException;
import java.net.URL;
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
    private Document dom;
    private Element root;

    public boolean open(final URL file) {
        // get the factory
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(file.openStream());
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
    public static List<Element> getElements(final Element el, final String tag) {
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
    public static String getTextValue(final Element el, final String tag) {
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
     * @param el
     *            Current element
     * @param tag
     *            Tag name
     * @return Integer value in tag, or null on failure
     */
    public static Integer getIntValue(final Element el, final String tag) {
        try {
            return Integer.parseInt(getTextValue(el, tag));
        } catch (final NumberFormatException nu) {

            System.err.print("Error converting to int in tag " + tag + ": "
                    + nu.getMessage());
        }

        return null;
    }

    /**
     * Calls getTextValue and returns a floating value
     * 
     * @param el
     *            Current element
     * @param tag
     *            Tag name
     * @return Floating value in tag, or null on failure
     */
    public static Float getFloatValue(final Element el, final String tag) {
        try {
            return Float.parseFloat(getTextValue(el, tag));
        } catch (final NumberFormatException nu) {

            System.err.print("Error converting to int in tag " + tag + ": "
                    + nu.getMessage());
        }

        return null;
    }

    public static Element getFirstElement(final Element element,
            final String tag) {
        final List<Element> elementList = getElements(element, tag);

        if (elementList != null && !elementList.isEmpty()) {
            return elementList.get(0);
        } else {
            return null;
        }
    }
}
