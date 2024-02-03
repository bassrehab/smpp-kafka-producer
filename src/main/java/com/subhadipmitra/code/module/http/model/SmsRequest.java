package com.subhadipmitra.code.module.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SMS submission request model.
 * Compatible with 5G SMSF (SMS Function) interface patterns.
 */
public class SmsRequest {

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("source")
    private String source;

    @JsonProperty("message")
    private String message;

    @JsonProperty("keyword")
    private String keyword;

    @JsonProperty("value")
    private String value;

    @JsonProperty("extraData")
    private String extraData;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("requestDeliveryReceipt")
    private boolean requestDeliveryReceipt;

    public SmsRequest() {
    }

    public SmsRequest(String destination, String message) {
        this.destination = destination;
        this.message = message;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isRequestDeliveryReceipt() {
        return requestDeliveryReceipt;
    }

    public void setRequestDeliveryReceipt(boolean requestDeliveryReceipt) {
        this.requestDeliveryReceipt = requestDeliveryReceipt;
    }

    /**
     * Parse the message field if it's in CSV format (keyword,value,extraData).
     */
    public void parseMessageIfCsv() {
        if (message != null && keyword == null) {
            String[] parts = message.split(",", 3);
            if (parts.length >= 1) {
                this.keyword = parts[0].trim();
            }
            if (parts.length >= 2) {
                this.value = parts[1].trim();
            }
            if (parts.length >= 3) {
                this.extraData = parts[2].trim();
            }
        }
    }

    @Override
    public String toString() {
        return "SmsRequest{" +
                "destination='" + destination + '\'' +
                ", source='" + source + '\'' +
                ", keyword='" + keyword + '\'' +
                ", priority=" + priority +
                '}';
    }
}
