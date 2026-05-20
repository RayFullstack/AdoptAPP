package com.adoptapp.sharedkernel.exception;

/**
 * Thrown when a remote service call fails.
 * Maps to HTTP 503 Service Unavailable.
 */
public class RemoteServiceException extends RuntimeException {

    private final String serviceName;

    public RemoteServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public RemoteServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
