package org.wso2.identity.event.ssf.publisher.api.exception;

public class SSFAdapterException extends Exception {

    private final String errorCode;

    private String description;

    public SSFAdapterException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

    public SSFAdapterException(String message, String description, String errorCode) {

        super(message);
        this.description = description;
        this.errorCode = errorCode;
    }

    public SSFAdapterException(String message, String description, String errorCode, Throwable cause) {

        super(message, cause);
        this.description = description;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public String getDescription() {

        return description;
    }
}
