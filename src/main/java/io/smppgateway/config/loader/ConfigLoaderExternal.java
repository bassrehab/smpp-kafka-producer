package io.smppgateway.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 05/07/17.
 *
 */
public class ConfigLoaderExternal {
    /** Logger Instance */
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderExternal.class);
    private final String propertiesFile;
    private final Properties prop;

    public ConfigLoaderExternal(String confFile) {
        this.propertiesFile = confFile;
        this.prop = new Properties();
        logger.info("Using config file: {}", confFile);
    }

    public List<String> getProperty(String property, String delimiter) {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            prop.load(input);
            String value = prop.getProperty(property);
            if (value == null) {
                logger.error("Property not found: {}", property);
                return null;
            }
            return Arrays.asList(value.split("\\s*" + delimiter + "\\s*"));
        } catch (IOException ex) {
            logger.error("Error reading property '{}' from config file: {}", property, propertiesFile, ex);
            return null;
        }
    }

    public String getProperty(String property) {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            prop.load(input);
            String value = prop.getProperty(property);
            if (value == null) {
                logger.error("Property not found: {}", property);
            }
            return value;
        } catch (IOException ex) {
            logger.error("Error reading property '{}' from config file: {}", property, propertiesFile, ex);
            return null;
        }
    }
}
