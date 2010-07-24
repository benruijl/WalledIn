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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The SettingsManager singleton can load and parse configuration files using
 * the Properties class. Settings can be looked up by their String value.
 * 
 * @author Ben Ruijl
 * 
 * @see Properties
 * 
 */
public final class SettingsManager {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(SettingsManager.class);
    /** Reference object. */
    private static SettingsManager ref = null;

    /** The parsed configuration data. */
    private final Properties config;

    /** Creates a new entity manager. */
    private SettingsManager() {
        config = new Properties();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Return the instance of the settings manager singleton.
     * 
     * @return Settings manager
     */
    public static SettingsManager getInstance() {
        if (ref == null) {
            ref = new SettingsManager();
        }

        return ref;
    }

    /**
     * Retrieves an Object from the settings list.
     * 
     * @param setting
     *            Name of the setting
     * @return An Object or null when the setting does not exist.
     */
    public Object get(final String setting) {
        return config.get(setting);
    }

    /**
     * Retrieves an integer from the settings list.
     * 
     * @param setting
     *            Name of the setting
     * @return An integer or null when the setting does not exist.
     */
    public Integer getInteger(final String setting) {
        return Integer.valueOf(config.getProperty(setting));
    }

    /**
     * Retrieves a string from the settings list.
     * 
     * @param setting
     *            Name of the setting
     * @return A string or null when the setting does not exist.
     */
    public String getString(final String setting) {
        return config.getProperty(setting);
    }

    /**
     * Retrieves a float from the settings list.
     * 
     * @param setting
     *            Name of the setting
     * @return A float or null when the setting does not exist.
     */
    public Float getFloat(final String setting) {
        return Float.valueOf(config.getProperty(setting));
    }

    /**
     * Retrieves a boolean from the settings list.
     * 
     * @param setting
     *            Name of the setting
     * @return A boolean or null when the setting does not exist.
     */
    public Boolean getBoolean(final String setting) {
        return Boolean.valueOf(config.getProperty(setting));
    }

    /**
     * Loads the settings from an URL.
     * 
     * @param scriptURL
     *            URL to configuration file
     * @throws IOException
     *             Throws an error when the loading or parsing fails.
     */
    public void loadSettings(final URL scriptURL) throws IOException {
        final Reader reader = new InputStreamReader(scriptURL.openStream());
        config.load(reader);
        reader.close();
    }
}
