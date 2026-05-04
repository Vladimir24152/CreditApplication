package org.neoflex.creditapplicationsupportstarter.exception;

import lombok.Getter;

@Getter
public class InternalServiceException extends RuntimeException {
    private final String serviceName;
    private final String message;
    private final HttpErrorInternalServiceResponse httpErrorInternalServiceResponse;

    public InternalServiceException(String serviceName, String message, HttpErrorInternalServiceResponse httpErrorInternalServiceResponse) {
        super(message);
        this.serviceName = serviceName;
        this.message = message;
        this.httpErrorInternalServiceResponse = httpErrorInternalServiceResponse;
    }
}
