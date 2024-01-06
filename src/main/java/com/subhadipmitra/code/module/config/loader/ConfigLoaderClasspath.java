package com.subhadipmitra.code.module.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 05/07/17.
 *
 */
public class ConfigLoaderClasspath {
    /** Logger Instance */
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderClasspath.class);

    private final String propertiesFile;
    private final Properties prop;

    public ConfigLoaderClasspath(String confFile) {
        this.propertiesFile = confFile;
        this.prop = new Properties();
    }

    public List<String> getProperty(String property, String delimiter) {
        try (InputStream input = ConfigLoaderClasspath.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                logger.error("Unable to find config file on classpath: {}", propertiesFile);
                return null;
            }
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
        try (InputStream input = ConfigLoaderClasspath.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                logger.error("Unable to find config file on classpath: {}", propertiesFile);
                return null;
            }
            prop.load(input);
            return prop.getProperty(property);
        } catch (IOException ex) {
            logger.error("Error reading property '{}' from config file: {}", property, propertiesFile, ex);
            return null;
        }
    }
}
