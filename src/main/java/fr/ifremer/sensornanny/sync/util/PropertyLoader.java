package fr.ifremer.sensornanny.sync.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyLoader {

    private static final Logger logger = Logger.getLogger(PropertyLoader.class.getName());

    public static Properties load(String propertyFile) {
        return load(propertyFile, null);
    }

    public static Properties load(String propertyFile, Properties properties) {
        InputStream inputStream = PropertyLoader.class.getClassLoader().getResourceAsStream(propertyFile);

        if (inputStream != null) {
            try {
                if (properties == null) {
                    properties = new Properties();
                }
                properties.load(inputStream);
            } catch (IOException e) {
                properties = null;
                logger.log(Level.SEVERE, "Error while reading '" + propertyFile + "'", e);
            }
        } else {
            String message = "Property file '" + propertyFile + "' not found in the classpath";
            logger.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
        }
        return properties;
    }
}
