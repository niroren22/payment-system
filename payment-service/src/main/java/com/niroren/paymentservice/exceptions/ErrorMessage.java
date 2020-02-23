package com.niroren.paymentservice.exceptions;

public class ErrorMessage {
    private int status;
    private String message;

    // For serialization
    public ErrorMessage() {}

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
