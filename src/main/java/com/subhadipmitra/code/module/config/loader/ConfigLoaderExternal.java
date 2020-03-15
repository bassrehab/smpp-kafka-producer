package com.subhadipmitra.code.module.config.loader;

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
    private static Logger logger = LoggerFactory.getLogger("ConfigLoaderExternal");
    private String properties_file;
    private Properties prop;

    public ConfigLoaderExternal(String conf_file){

        this.properties_file = conf_file;
        this.prop = new Properties();

        logger.info("Using config file:" + conf_file);

    }

    public List<String> getProperty(String property, String delimiter) {

        List<String> property_list = null;
        InputStream input = null;


        try {

            input = new FileInputStream(properties_file);
            if(input==null){
                logger.error("Sorry, unable to find config file: " + properties_file);
                return property_list;
            }
            prop.load(input);

            property_list = Arrays.asList(prop.get(property).toString().split("\\s*"+ delimiter +"\\s*"));



        } catch (IOException ex) {
            ex.printStackTrace();


        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        return property_list;

    }


    public String getProperty(String property){
        InputStream input = null;

        String property_value;

        try{
            input = new FileInputStream(properties_file);
            if(input==null){
                logger.error("Sorry, unable to find: " + properties_file);
                return null;
            }

            //load a properties file from class path, inside static method
            prop.load(input);
            property_value = prop.get(property).toString();
            return property_value;


        } catch (IOException ex) {

            ex.printStackTrace();
            return null;

        }



    }


}
