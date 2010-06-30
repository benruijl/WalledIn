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

import java.net.URL;

public class Utils {
    /** A small value to prevent rounding errors */
    private static final float EPSILON = 0.00001f;

    public static URL getClasspathURL(final String filename) {
        return ClassLoader.getSystemResource(filename);
    }

    public static boolean equals(final float a, final float b) {
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }

    /**
     * Clamps an integer to a range
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
}