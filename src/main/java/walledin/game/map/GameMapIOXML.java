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
package walledin.game.map;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.util.XMLReader;

/**
 * Loads a map from an XML file
 * 
 * @author ben
 */
public class GameMapIOXML implements GameMapIO {
    // FIXME dont use instance vars for this...
    private int width;
    private int height;
    private final EntityManager entityManager;

    public GameMapIOXML(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Reads tile information
     * 
     * @param reader
     *            XML reader
     * @param element
     *            Current element
     * @return An list of tiles
     */
    private List<Tile> parseTiles(final Element element) {
        final List<Tile> result = new ArrayList<Tile>();
        final String tiles = XMLReader.getTextValue(element, "tiles");
        final String[] rows = tiles.split("\n");
        int x = 0;
        int y = 0;
        for (final String row : rows) {
            if (row.trim().isEmpty()) {
                continue;
            }
            y = 0;
            for (final char mapChar : row.trim().toCharArray()) {
                final TileType type = TileType.getTile(mapChar);
                final Tile tile = new Tile(type, x, y);
                result.add(tile);
                y++;
            }
            x++;
        }
        height = x;
        width = y;
        return result;
    }

    private List<Entity> parseItems(final Element element) {
        final List<Entity> itList = new ArrayList<Entity>();
        final Element itemsNode = XMLReader.getFirstElement(element, "items");
        final List<Element> items = XMLReader.getElements(itemsNode, "item");

        for (final Element el : items) {
            final String name = el.getAttribute("name");
            final String type = el.getAttribute("type");
            final int x = Integer.parseInt(el.getAttribute("x"));
            final int y = Integer.parseInt(el.getAttribute("y"));

<<<<<<< HEAD
            final Entity item = entityManager.create(Enum.valueOf(Family.class,
                    type), name);
=======
            final Entity item = entityManager.create(type, name);
>>>>>>> master
            item.setAttribute(Attribute.POSITION, new Vector2f(x, y));
            itList.add(item);
        }

        return itList;
    }

    /**
     * Reads map data from an XML file
     * 
     * @param filename
     *            Filename of XML data
     * @return Returns true on success and false on failure
     */
    @Override
    public Entity readFromURL(final URL file) {
        final XMLReader reader = new XMLReader();

        if (reader.open(file)) {
            final Element mapElement = reader.getRootElement();

            final String name = XMLReader.getTextValue(mapElement, "name");
            final List<Entity> items = parseItems(mapElement);
            final List<Tile> tiles = parseTiles(mapElement);

<<<<<<< HEAD
            final Entity map = entityManager.create(Family.MAP, name);
=======
            final Entity map = entityManager.create("Map", name);
>>>>>>> master
            map.setAttribute(Attribute.WIDTH, width);
            map.setAttribute(Attribute.HEIGHT, height);
            map.setAttribute(Attribute.TILES, tiles);
            map.setAttribute(Attribute.ITEM_LIST, items);

            return map;
        } else {
            return null;
        }

    }

    @Override
    public boolean writeToFile(final Entity map, final String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
