package com.example.jmsrestapi.models;

import java.time.LocalDateTime;

public class StandardResponse {
    private String correlationId;
    private Object receivedObject;
    private LocalDateTime receiveDate;

    public StandardResponse() {
    }

    public StandardResponse(String correlationId, Object receivedObject, LocalDateTime receiveDate) {
        this.correlationId = correlationId;
        this.receivedObject = receivedObject;
        this.receiveDate = receiveDate;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Object getReceivedObject() {
        return receivedObject;
    }

    public void setReceivedObject(Object receivedObject) {
        this.receivedObject = receivedObject;
    }

    public LocalDateTime getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(LocalDateTime receiveDate) {
        this.receiveDate = receiveDate;
    }
}
