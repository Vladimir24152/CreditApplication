package org.neoflex.deal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
public class HttpErrorInternalServiceResponse {

    private final String header;

    private final int code;

    private final String type;

    private final LocalDateTime timestamp;

    private final ServiceErrorMessage serviceErrorMessage;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ServiceErrorMessage {

        private final String serviceName;

        private final String message;

        private final HttpErrorResponse httpErrorResponse;
    }
}
