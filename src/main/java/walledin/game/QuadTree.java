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
package walledin.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class QuadTree {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(QuadTree.class);

    /** Maximum depth of the tree. */
    public static final int MAX_DEPTH = 5;
    /**
     * Maximum objects per leaf if the depth is less than the maximum depth.
     */
    public static final int MAX_OBJECTS = 6;

    private final QuadTree[] children;
    private final List<Entity> objects;
    private final Rectangle rectangle;
    private final int depth;
    private boolean leaf;

    public QuadTree(final Rectangle rect) {
        this(rect, 0);
    }

    private QuadTree(final Rectangle rect, final int depth) {
        children = new QuadTree[4];
        objects = new ArrayList<Entity>();
        rectangle = rect;
        leaf = true;
        this.depth = depth;
    }

    private void addObject(final Entity object) {
        objects.add(object);

        if (objects.size() > MAX_OBJECTS) {
            subdivide();
        }
    }

    private void removeObject(final Entity object) {
        objects.remove(object);
    }

    private boolean containsFully(final Entity object) {
        final Rectangle rect = ((AbstractGeometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        return rectangle.containsFully(rect);
    }

    private boolean containsFully(final Rectangle rect) {
        return rectangle.containsFully(rect);
    }

    public void add(final Entity object) {
        final Rectangle rect = ((AbstractGeometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        final QuadTree tree = getSmallestQuadTreeContainingRectangle(rect);

        if (tree == null) {
            LOG.warn("Could not add object to the quadtree, because it is out of the bounds: "
                    + rect.getLeftTop() + " - " + rect.getRightBottom());
        } else {
            addObject(object);
        }
    }

    private QuadTree getSmallestQuadTreeContainingRectangle(final Rectangle rect) {
        /* If this is a leaf and it does not contain the object, return null. */

        if (!leaf) {
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(rect)) {
                    final QuadTree tree = children[i]
                            .getSmallestQuadTreeContainingRectangle(rect);

                    if (tree != null) {
                        return tree;
                    }
                }
            }
        }

        if (containsFully(rect)) {
            return this;
        }

        return null;
    }

    public QuadTree getQuadTreeContainingObject(final Entity object) {
        final Rectangle rect = ((AbstractGeometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        if (objects.contains(object)) {
            return this;
        }

        /* If this is a leaf and it does not contain the object, return null. */
        if (leaf) {
            return null;
        }

        for (int i = 0; i < 4; i++) {
            if (children[i].containsFully(rect)) {
                final QuadTree tree = children[i]
                        .getQuadTreeContainingObject(object);

                if (tree != null) {
                    return tree;
                }
            }
        }

        return null;
    }

    public List<Entity> getObjectsFromRectangle(final Rectangle rect) {
        final List<Entity> objectList = new ArrayList<Entity>();

        if (rectangle.containsFully(rect)) {
            objectList.addAll(getObjects());
        }

        if (!leaf) {
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(rect)) {
                    final List<Entity> tree = children[i]
                            .getObjectsFromRectangle(rect);

                    if (tree != null) {
                        objectList.addAll(tree);
                        return objectList;
                    }
                }
            }
        }

        return objectList;
    }

    public List<Entity> getObjects() {
        return objects;
    }

    public void remove(final Entity object) {
        final QuadTree tree = getQuadTreeContainingObject(object);

        if (tree != null) {
            tree.removeObject(object);
        }
    }

    /**
     * Subdivides this leaf into four rectangles.
     */
    private void subdivide() {
        if (!leaf || depth + 1 >= MAX_DEPTH) {
            return;
        }

        leaf = false;

        final Vector2f size = new Vector2f(rectangle.getWidth() / 2.0f,
                rectangle.getHeight() / 2.0f);
        final Vector2f middle = rectangle.getLeftTop().add(size);

        children[0] = new QuadTree(new Rectangle(rectangle.getLeft(),
                rectangle.getTop(), size.getX(), size.getY()), depth + 1);
        children[1] = new QuadTree(new Rectangle(rectangle.getLeft(),
                middle.getY(), size.getX(), size.getY()), depth + 1);
        children[2] = new QuadTree(new Rectangle(middle.getX(), middle.getY(),
                size.getX(), size.getY()), depth + 1);
        children[3] = new QuadTree(new Rectangle(middle.getX(),
                rectangle.getTop(), size.getX(), size.getY()), depth + 1);

        final Iterator<Entity> it = objects.iterator();
        while (it.hasNext()) {
            final Entity object = it.next();
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(object)) {
                    children[i].addObject(object);
                    it.remove();
                }
            }
        }
    }
}
