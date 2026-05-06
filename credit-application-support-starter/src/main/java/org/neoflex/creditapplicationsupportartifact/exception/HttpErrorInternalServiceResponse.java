package org.neoflex.creditapplicationsupportartifact.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class HttpErrorInternalServiceResponse {

    private final int code;

    private final String type;

    private final LocalDateTime timestamp;

    private final String message;

    private final ServiceErrorMessage serviceErrorMessage;

    @Getter
    @AllArgsConstructor
    public static class ServiceErrorMessage {

        private final String serviceName;

        private final String message;

        private final HttpErrorInternalServiceResponse httpErrorInternalServiceResponse;
    }
}
