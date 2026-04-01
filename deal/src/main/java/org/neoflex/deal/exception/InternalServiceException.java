package org.neoflex.deal.exception;

import lombok.Getter;
import org.neoflex.deal.dto.response.HttpErrorResponse;

@Getter
public class InternalServiceException extends RuntimeException {
    private final String serviceName;
    private final String message;
    private final HttpErrorResponse httpErrorResponse;

    public InternalServiceException(String serviceName,String message, HttpErrorResponse httpErrorResponse) {
        super(message);
        this.serviceName = serviceName;
        this.message = message;
        this.httpErrorResponse = httpErrorResponse;
    }
}
