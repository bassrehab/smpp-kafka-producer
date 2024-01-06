package com.subhadipmitra.code.module.models;

import com.subhadipmitra.code.module.events.service.eventrecord.EventRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.subhadipmitra.code.module.config.initialize.ConfigurationsSourceSMPP.SOURCE_SMPP_SMS_DELIMITER;
import static com.subhadipmitra.code.module.init.Main.utilities;

/**
 * Class that Models the SMS object with other necessary details.
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 11/10/17.
 *
 */
public class SMS {
    private static final String DELIMITER = SOURCE_SMPP_SMS_DELIMITER;
    /** Thread-safe date formatter */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss.SSS");
    private String msisdn; // Destination Address
    private String keyword;
    private String value;
    private String extradata; // for additional payload -- //
    private String timestamp;
    private String has_parsing_errors;
    private String payload;

    /**
     * Constructor
     * @param evt SMS Event Wrap Obj
     */
    public SMS(EventRecord evt) {

        this.msisdn = evt.getEvt().getDestAddress().getAddress();

        // Call SMS Parser and Assign members
        SMSParser(new String(evt.getEvt().getShortMessage()));


    }

    /**
     * Parse the SMS
     * @param sms sms text body
     */
    private void SMSParser(String sms){

        this.keyword = this.value = this.timestamp = "";

        try{
            List<String> smsContent =  utilities.splitRecord(sms, DELIMITER);
            this.keyword = smsContent.get(0).trim();
            this.value = smsContent.get(1).trim();

            // for additional payload -- //

            try{
                this.extradata =  smsContent.get(2).trim();
            }
            catch (Exception e){
                this.extradata = "";
            }


            this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            this.has_parsing_errors = "FALSE";

        }
        catch (Exception e){
            this.has_parsing_errors = "TRUE";
        }

        String COMMA = ",";

        this.payload = this.msisdn + COMMA
                + this.keyword + COMMA
                + this.value + COMMA
                + this.extradata + COMMA
                + this.timestamp;

    }


    /** Default Constructor */
    public SMS(String msisdn, String keyword, String value, String extradata, String timestamp, String has_parsing_errors, String payload) {
        this.msisdn = msisdn;
        this.keyword = keyword;
        this.value = value;
        this.extradata = extradata;
        this.timestamp = timestamp;
        this.has_parsing_errors = has_parsing_errors;
        this.payload = payload;
    }



    public static String getDELIMITER() {
        return DELIMITER;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHas_parsing_errors() {
        return has_parsing_errors;
    }

    public void setHas_parsing_errors(String has_parsing_errors) {
        this.has_parsing_errors = has_parsing_errors;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getExtradata() {
        return extradata;
    }

    public void setExtradata(String extradata) {
        this.extradata = extradata;
    }

    @Override
    public String toString() {
        return "SMS[" +
                "msisdn='" + msisdn + '\'' +
                ", keyword='" + keyword + '\'' +
                ", value='" + value + '\'' +
                ", extradata='" + extradata + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", has_parsing_errors='" + has_parsing_errors + '\'' +
                ", payload='" + payload + '\'' +
                ']';
    }
}
