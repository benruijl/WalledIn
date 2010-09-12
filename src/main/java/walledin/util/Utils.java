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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A helper library for WalledIn.
 * 
 * @author Ben Ruijl, Wouter Smeenk
 * 
 */
public final class Utils {
    /** A small value to prevent rounding errors. */
    private static final float EPSILON = 0.00001f;

    /** Private constructor. Never called. */
    private Utils() {

    }

    /**
     * Gets the class path of a resource.
     * 
     * @param name
     *            Name of resource
     * @return The <code>URL</code> of the resource
     */
    public static URL getClasspathURL(final String name) {
        return ClassLoader.getSystemResource(name);
    }

    /**
     * Safe float comparison. Uses a small error, defined by
     * <code>EPSILON</code>.
     * 
     * @param a
     *            First float
     * @param b
     *            Second float
     * @return Returns true if equal within the error, else false.
     */
    public static boolean equals(final float a, final float b) {
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }

    /**
     * Clamps an integer to a range.
     * 
     * @param x
     *            Integer to clamp
     * @param min
     *            Minimum value
     * @param max
     *            Maximum value
     * @return Clamped integer
     */
    public static float clamp(final float x, final float min, final float max) {
        return x < min ? min : x > max ? max : x;
    }

    /**
     * Returns the half of the circle in which the angle lies.
     * 
     * @param angle
     *            Angle
     * @return Returns -1 if in the left half of the circle and 1 if in the
     *         right half
     */
    public static int getCircleHalf(final float angle) {
        return angle > 0.5 * Math.PI && angle < 1.5 * Math.PI ? -1 : 1;
    }

    /**
     * Creates an independent copy of the boolean array. This involves cloning
     * the structure and the data itself.
     * 
     * @param array
     *            The array to be cloned
     * @return An independent 'deep' structure clone of the array
     */
    public static boolean[][] clone2DArray(final boolean[][] array) {
        final int rows = array.length;

        final boolean[][] newArray = array.clone();

        for (int row = 0; row < rows; row++) {
            newArray[row] = array[row].clone();
        }

        return newArray;
    }

    /**
     * Reverses the mapping
     * 
     * @param <V>
     *            Value of the original map, key of the new map
     * @param <K>
     *            Key of the original map, value of the new map
     * @param map
     *            The map to be reversed
     * @return A reversed map
     */
    public static <V, K> Map<V, K> reverseMap(final Map<K, V> map) {
        final Map<V, K> result = new HashMap<V, K>();
        for (final Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    /**
     * Represents the address as string
     * 
     * @param address
     *            The address
     * @return The string that represents the address
     */
    public static String getAddressRepresentation(final SocketAddress address) {
        final InetSocketAddress inetAddr = (InetSocketAddress) address;
        return inetAddr.getAddress().getHostAddress() + "@"
                + inetAddr.getPort();
    }
}
