package com.subhadipmitra.code.module.models;

/**
 * Created by Subhadip Mitra <contact@subhadipmitra.com>  on 28/08/17.
 */
public class SRC_MapTelemetry {

    public String batchId;
    public String batchWindow;
    public String srcName;
    public String srcId;
    public long recordsProcessed;
    public String firstRecordId;
    public long landingTime;

    /** Default Constructor */
    public SRC_MapTelemetry(String batchId, String batchWindow, String srcName,
                            String srcId, long recordsProcessed, String firstRecordId,
                            long landingTime) {

        this.batchId = batchId;
        this.batchWindow = batchWindow;
        this.srcName = srcName;
        this.srcId = srcId;
        this.recordsProcessed = recordsProcessed;
        this.firstRecordId = firstRecordId;
        this.landingTime = landingTime;
    }

    @Override
    public String toString() {
        return "SRC_MapTelemetry{" +
                "batchId='" + batchId + '\'' +
                ", batchWindow='" + batchWindow + '\'' +
                ", srcName='" + srcName + '\'' +
                ", srcId='" + srcId + '\'' +
                ", recordsProcessed=" + recordsProcessed +
                ", firstRecordId='" + firstRecordId + '\'' +
                ", landingTime=" + landingTime +
                '}';
    }
}
