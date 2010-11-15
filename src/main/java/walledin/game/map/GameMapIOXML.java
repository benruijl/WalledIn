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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import walledin.engine.math.Vector2f;
import walledin.game.ClientLogicManager;
import walledin.game.EntityManager;
import walledin.game.ItemInfo;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.behaviors.logic.ItemManagementBevahior;
import walledin.util.XMLReader;

/**
 * Loads a map from an XML file.
 * 
 * @author Ben Ruijl
 */
public final class GameMapIOXML implements GameMapIO {
    // FIXME dont use instance vars for this...
    private int width;
    private int height;

    /**
     * Reads tile information.
     * 
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
            x = 0;
            for (final char mapChar : row.trim().toCharArray()) {
                final TileType type = TileType.getTile(mapChar);
                final Tile tile = new Tile(type, x, y);
                result.add(tile);
                x++;
            }
            y++;
        }
        height = y;
        width = x;
        return result;
    }

    /**
     * Parses the item behaviors from a map file.
     * 
     * @param element
     *            XML element
     * @return A set of item information
     */
    private Set<ItemInfo> parseItems(final Element element,
            final float tileWidth) {
        final Set<ItemInfo> itList = new HashSet<ItemInfo>();
        final Element itemsNode = XMLReader.getFirstElement(element, "items");
        final List<Element> items = XMLReader.getElements(itemsNode, "item");

        for (final Element el : items) {
            final String name = el.getAttribute("name"); // TODO: use name?
            final String type = el.getAttribute("type");
            final int x = Integer.parseInt(el.getAttribute("x"));
            final int y = Integer.parseInt(el.getAttribute("y"));
            final int respawnTime = Integer
                    .parseInt(el.getAttribute("respawn"));
            Vector2f pos = new Vector2f(x, y).scale(tileWidth);
            pos = pos.scale(1 / ClientLogicManager.PIXELS_PER_METER);
            final ItemInfo item = new ItemInfo(
                    Enum.valueOf(Family.class, type), pos, respawnTime);

            itList.add(item);
        }

        return itList;
    }

    /**
     * Reads spawn points from the map file.
     * 
     * @param element
     *            Current element
     * @return List of spawn points
     */
    private List<SpawnPoint> readSpawnPoints(final Element element,
            final float tileWidth) {
        final List<SpawnPoint> points = new ArrayList<SpawnPoint>();

        final Element node = XMLReader.getFirstElement(element, "spawnpoints");
        final List<Element> spawnElems = XMLReader.getElements(node, "spawn");

        for (final Element el : spawnElems) {
            final int x = Integer.parseInt(el.getAttribute("x"));
            final int y = Integer.parseInt(el.getAttribute("y"));
            points.add(new SpawnPoint(new Vector2f(x, y).scale(tileWidth)));
        }

        return points;
    }

    /**
     * Reads map data from an XML file.
     * 
     * @param entityManager
     *            Used for creating entities
     * @param file
     *            URL of XML resource
     * @return Returns true on success and false on failure
     */
    @Override
    public Entity readFromURL(final EntityManager entityManager, final URL file) {
        final XMLReader reader = new XMLReader();

        if (reader.open(file)) {
            final Element mapElement = reader.getRootElement();

            final String name = XMLReader.getTextValue(mapElement, "name");
            final Float tileWidth = XMLReader.getFloatValue(mapElement,
                    "tileWidth");
            final Set<ItemInfo> items = parseItems(mapElement, tileWidth);
            final List<SpawnPoint> points = readSpawnPoints(mapElement,
                    tileWidth);
            final List<Tile> tiles = parseTiles(mapElement);
            final Entity map = entityManager.create(Family.MAP, name);

            map.addBehavior(new ItemManagementBevahior(map, items));
            map.setAttribute(Attribute.WIDTH, width);
            map.setAttribute(Attribute.HEIGHT, height);
            map.setAttribute(Attribute.SPAWN_POINTS, points);
            map.setAttribute(Attribute.TILES, tiles);
            map.setAttribute(Attribute.TILE_WIDTH, tileWidth);

            return map;
        } else {
            return null;
        }

    }

    @Override
    public boolean writeToFile(final Entity map, final String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Tile> readTilesFromURL(final URL file) {
        final XMLReader reader = new XMLReader();

        if (reader.open(file)) {
            final Element mapElement = reader.getRootElement();
            final List<Tile> tiles = parseTiles(mapElement);
            return tiles;
        } else {
            return null;
        }
    }
}
