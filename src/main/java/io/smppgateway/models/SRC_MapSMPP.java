package io.smppgateway.models;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 20/08/17.
 *
 * JSON Mapper class for consuming SMPP events
 * This is only a SAMPLE Mapper, define your own mapper
 */
public class SRC_MapSMPP {

    /** Common fields.*/
    public String id;
    public String srcId;

    /** processor would contain the full SOAP request in CSV */
    public String payload;
    public long entryTime;
    public String isProcessed;
    public String batchId;

    public String has_parsing_errors;

    /** Specifics from SMPP reqs.
     * If these contain errors,
     * this shall be set to null or ''*/
    public String msisdn;
    public String keyword;
    public String value;
    public String extradata;
    public String timestamp;


    public SRC_MapSMPP() {}

    /** Constructor */
    public SRC_MapSMPP(String id, String srcId, String payload, long entryTime, String isProcessed, String batchId,
                       String has_parsing_errors, String msisdn, String keyword, String value, String extradata, String timestamp) {
        this.id = id;
        this.srcId = srcId;
        this.payload = payload;
        this.entryTime = entryTime;
        this.isProcessed = isProcessed;
        this.batchId = batchId;
        this.has_parsing_errors = has_parsing_errors;
        this.msisdn = msisdn;
        this.keyword = keyword;
        this.value = value;
        this.extradata = extradata;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SRC_MapSMPP{" +
                "id='" + id + '\'' +
                ", srcId='" + srcId + '\'' +
                ", payload='" + payload + '\'' +
                ", entryTime=" + entryTime +
                ", isProcessed='" + isProcessed + '\'' +
                ", batchId='" + batchId + '\'' +
                ", has_parsing_errors='" + has_parsing_errors + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", keyword='" + keyword + '\'' +
                ", value='" + value + '\'' +
                ", extradata='" + extradata + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
