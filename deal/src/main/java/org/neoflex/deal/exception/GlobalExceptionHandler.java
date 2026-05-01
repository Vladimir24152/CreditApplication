package org.neoflex.deal.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.exception.BaseGlobalExceptionHandler;
import org.neoflex.creditapplicationsupportstarter.exception.HttpErrorInternalServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleEntityNotFoundException(
            EntityNotFoundException e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.NOT_FOUND,
                "ENTITY_NOT_FOUND",
                e.getMessage()
        );
    }

    @ExceptionHandler(CodeVerificationException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleCodeVerificationException(
            EntityNotFoundException e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.FORBIDDEN,
                "ENTITY_NOT_FOUND",
                e.getMessage()
        );
    }
}
