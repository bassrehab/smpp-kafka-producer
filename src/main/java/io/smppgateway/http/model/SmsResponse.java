package io.smppgateway.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SMS submission response model.
 * Compatible with 5G SMSF (SMS Function) interface patterns.
 */
public class SmsResponse {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private String timestamp;

    public SmsResponse() {
    }

    public static SmsResponse success(String messageId, String timestamp) {
        SmsResponse response = new SmsResponse();
        response.setMessageId(messageId);
        response.setStatus("ACCEPTED");
        response.setStatusCode(202);
        response.setMessage("Message accepted for delivery");
        response.setTimestamp(timestamp);
        return response;
    }

    public static SmsResponse error(int statusCode, String errorMessage) {
        SmsResponse response = new SmsResponse();
        response.setStatus("ERROR");
        response.setStatusCode(statusCode);
        response.setMessage(errorMessage);
        return response;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SmsResponse{" +
                "messageId='" + messageId + '\'' +
                ", status='" + status + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
