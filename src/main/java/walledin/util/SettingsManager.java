package walledin.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class SettingsManager {
    private static final Logger LOG = Logger.getLogger(SettingsManager.class);
    private static SettingsManager ref = null;
    Properties config;

    private SettingsManager() {
        config = new Properties();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static SettingsManager getInstance() {
        if (ref == null) {
            ref = new SettingsManager();
        }

        return ref;
    }
    
    public Object get(String setting) {
        return config.get(setting);
    }
    
    public Integer getInteger(String setting) {
        return Integer.valueOf(config.getProperty(setting));
    }
    
    public String getString(String setting) {
        return config.getProperty(setting);
    }
    
    public Float getFloat(String setting) {
        return Float.valueOf(config.getProperty(setting));
    }
    
    public Boolean getBoolean(String setting) {
        return Boolean.valueOf(config.getProperty(setting));
    }
    
    public void loadSettings(final URL scriptURL) throws IOException {     
        final Reader reader = new InputStreamReader(scriptURL.openStream());
        config.load(reader);
    }
}
