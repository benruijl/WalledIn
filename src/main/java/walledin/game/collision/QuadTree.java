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
package walledin.game.collision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

/**
 * A quadtree for spatial partitioning.
 * 
 * @author Ben Ruijl
 * 
 */
public class QuadTree {
    /** Maximum depth of the tree. */
    public static final int MAX_DEPTH = 5;
    /**
     * Maximum objects per leaf if the depth is less than the maximum depth.
     */
    public static final int MAX_OBJECTS = 6;

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(QuadTree.class);

    /**
     * Children quadtrees. There are either none or four, depending on whether
     * this quadtree is a leaf or not.
     */
    private final QuadTree[] children;

    /** Objects in this node or leaf. */
    private final List<StaticObject> objects;

    /** Bounding rectangle. */
    private final Rectangle rectangle;

    /** Depth in the hierarchy. */
    private final int depth;

    /** Is this quadtree a leaf? */
    private boolean leaf;

    /**
     * Is this quadtree a leaf? private boolean leaf;
     * 
     * /** Creates a new quadtree that begins a new hierarchy.
     * 
     * @param rect
     *            Rectangle of this quadtree
     */
    public QuadTree(final Rectangle rect) {
        this(rect, 0);
    }

    /**
     * Creates a new quadtree leaf or node.
     * 
     * @param rect
     *            Rectangle of this quadtree
     * @param depth
     *            Position of this quadtree in the hierarchy.
     */
    private QuadTree(final Rectangle rect, final int depth) {
        children = new QuadTree[4];
        objects = new ArrayList<StaticObject>();
        rectangle = rect;
        leaf = true;
        this.depth = depth;
    }

    /**
     * Adds an object to the object list of the current node or leaf.
     * 
     * @param object
     *            Object to add
     */
    private void addObject(final StaticObject object) {
        objects.add(object);

        if (objects.size() > MAX_OBJECTS) {
            subdivide();
        }
    }

    /**
     * Removes an object from the object list of the current node or leaf.
     * 
     * @param object
     *            Object to remove
     */
    private void removeObject(final StaticObject object) {
        objects.remove(object);
    }

    /**
     * Checks if the object is fully contained in the rectangle of this node or
     * leaf.
     * 
     * @param object
     *            Object
     * @return True if fully contained, else false
     */
    private boolean containsFully(final StaticObject object) {
        final Rectangle rect = object.getBoudingRectangle();
        return rectangle.containsFully(rect);
    }

    /**
     * Checks if a rectangle is fully contained in the rectangle of this node or
     * leaf.
     * 
     * @param rect
     *            Rectangle
     * @return True if fully contained, else false
     */
    private boolean containsFully(final Rectangle rect) {
        return rectangle.containsFully(rect);
    }

    /**
     * Checks if a rectangle is overlapping with the rectangle of this node or
     * leaf.
     * 
     * @param rect
     *            Rectangle
     * @return True if it is overlapping, else false
     */
    private boolean contains(final Rectangle rect) {
        return rectangle.intersects(rect);
    }

    /**
     * Adds an object to the quadtree if it is fully contained.
     * 
     * @param object
     *            Object to add
     */
    public void add(final StaticObject object) {
        final Rectangle rect = object.getBoudingRectangle();
        final QuadTree tree = getSmallestQuadTreeContainingRectangle(rect);

        if (tree == null) {
            LOG.warn("Could not add object to the quadtree, because it is out of the bounds: "
                    + rect.getLeftTop() + " - " + rect.getRightBottom());
        } else {
            addObject(object);
        }
    }

    /**
     * Get the smallest quadtree that <i>fully</i> contains a rectangle.
     * 
     * @param rect
     *            Rectangle to check
     * @return Smallest quadtree or null if none is found
     */
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

    /**
     * Finds the quadtree node or leaf that contains an object.
     * 
     * @param object
     *            Object
     * @return Quadtree or null on failure
     */
    public QuadTree getQuadTreeContainingObject(final StaticObject object) {
        final Rectangle rect = object.getBoudingRectangle();

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

    /**
     * Finds all the objects that could possibly be in the given rectangle.
     * 
     * @param rect
     *            Rectangle
     * @return List of entities
     */
    public List<StaticObject> getObjectsFromRectangle(final Rectangle rect) {
        final List<StaticObject> objectList = new ArrayList<StaticObject>();

        if (contains(rect)) {
            objectList.addAll(getObjects());
        } else {
            return null;
        }

        if (!leaf) {
            for (int i = 0; i < 4; i++) {
                final List<StaticObject> tree = children[i]
                        .getObjectsFromRectangle(rect);

                if (tree != null) {
                    objectList.addAll(tree);
                }
            }
        }

        return objectList;
    }

    /**
     * Returns the objects of the current node or leaf.
     * 
     * @return List of objects
     */
    public List<StaticObject> getObjects() {
        return objects;
    }

    /**
     * Recursively removes an object from the quadtree.
     * 
     * @param object
     *            Object to remove
     */
    public void remove(final StaticObject object) {
        final QuadTree tree = getQuadTreeContainingObject(object);

        if (tree != null) {
            tree.removeObject(object);
        }
    }

    /**
     * Subdivides this leaf into four children.
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

        final Iterator<StaticObject> it = objects.iterator();
        while (it.hasNext()) {
            final StaticObject object = it.next();
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(object)) {
                    children[i].addObject(object);
                    it.remove();
                }
            }
        }
    }
}
