package org.wso2.identity.event.ssf.publisher.api.exception;

public class SSFAdapterServerException extends SSFAdapterException {

    public SSFAdapterServerException(String message, String errorCode) {

        super(message, errorCode);
    }

    public SSFAdapterServerException(String message, String description, String errorCode, Throwable throwable) {

        super(message, description, errorCode, throwable);
    }
}
