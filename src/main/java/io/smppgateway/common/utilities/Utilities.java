package io.smppgateway.common.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 01/07/17.
 *
 */
public class Utilities {
    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    public boolean isFileCompletelyWritten(String fileAbsPath) {
        File file = new File(fileAbsPath);

        try (RandomAccessFile stream = new RandomAccessFile(file, "rw")) {
            return true;
        } catch (Exception e) {
            logger.debug("Skipping file {} - not completely written yet", file.getName());
        }
        return false;
    }



    public boolean isFile(String file_abs_path){
        Path watchedfile = new File(file_abs_path).toPath();
        return ( Files.exists(watchedfile) &&
                !Files.isDirectory(watchedfile) &&
                Files.isRegularFile(watchedfile) );
    }


    public String getCurrentTime(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }


    public List<String> splitCSVRecord(String str){
        try {
            // -1 to deal with trailing null values in CSV
            return Arrays.asList(str.split(",", -1));
        }
        catch (IndexOutOfBoundsException obe){
            logger.error("Error splitting CSV record: {}", str, obe);
            return null;
        }
    }

    public List<String> splitRecord(String str, String DELIMITER){

        return Arrays.asList(str.split(DELIMITER, -1));

    }


    /**
     * Checks that String is Not Null and Not Empty
     * @param str  String
     *
     * */


    public boolean validNonNullEmpty(String str){

        return str != null && !str.isEmpty();

    }



    /**
     * Checks that a string is numerical and non empty.
     *
     * */
    public boolean validOnlyNonNullNumbers(String text) {

        // Also validates that it is not null or empty.
        return text.matches("[0-9]+") && text.length() > 0;

    }


    /**
     * Overloaded.
     *
     * Checks that a string is numerical and non empty.
     *
     * */
    public boolean validOnlyNonNullNumbers(String text, int LENGTH) {

        // Also validates that it is not null or empty.
        return text.matches("[0-9]+") && text.length() > LENGTH;

    }

    /**
     * Checks that a string has yyyy-MM-dd HH:mm:ss.
     *
     * */

    public boolean validDateTimestamp(String ts){
        return Pattern.matches("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) ([2][0-3]|[0-1][0-9]|[1-9]):[0-5][0-9]:([0-5][0-9]|[6][0])$",ts);
    }


    /**
     * Computes the Cell Site Key from the cgi, sai, ecgi
     * @param cgi
     * @param sai
     * @param ecgi
     * @return cell site key.
     */

    public String computeCellSiteKey(String cgi, String sai, String ecgi) {

        if (cgi != null && cgi.length() >= 14) {

            String lacid = Integer.valueOf(cgi.substring(6, 10), 16).toString();
            String cellid = String.format("%05d", Integer.valueOf(cgi.substring(10, 14), 16));

            return lacid + cellid.substring(1, 5);

        } else if (sai != null && sai.length() >= 14) {

            String lacid = Integer.valueOf(sai.substring(6, 10), 16).toString();
            String cellid = String.format("%04d", Integer.valueOf(sai.substring(10, 14), 16));

            return lacid + cellid.substring(0, 4);

        } else if (ecgi != null && ecgi.length() >= 11) {

            String cellid = Integer.valueOf(ecgi.substring(6, 11), 16).toString();

            return cellid;
        } else
            return "";

    }


    /**
     * Searches a List for particular term.
     * @param LIST
     * @param searchTerm
     * @return
     */
    public boolean searchList(List<String> LIST, String searchTerm ){

        return LIST.stream().anyMatch(str -> str.trim().equals(searchTerm));

    }



    /**
     * compute operator code
     * @param IMSI
     *
     */

    public String computeOperator(String IMSI) {
        if((IMSI.substring(0,5)).contains("41001"))
        {
            return "Jazz";
        }

        else if ((IMSI.substring(0,5)).contains("41007"))

        {
            return "Warid";
        }

        return "";

    }


    /**
     * Banner Printing Utility
     * @param content Banner Content
     */

    public StringBuilder printBanner(String content){
        int bannerWidth = 150;
        String hemChar = "*";
        String newLine = "\n";

        StringBuilder banner = new StringBuilder();

        for (int i = 0; i < bannerWidth; i++) {
            banner.append(hemChar);
        }

        banner.append(newLine);
        banner.append(content);
        banner.append(newLine);


        for (int i = 0; i < bannerWidth; i++) {
            banner.append(hemChar);
        }
        return banner;

    }

}
