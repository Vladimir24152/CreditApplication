package org.neoflex.deal.exception;

import jakarta.persistence.EntityNotFoundException;
import org.neoflex.creditapplicationsupportartifact.exception.BaseGlobalExceptionHandler;
import org.neoflex.creditapplicationsupportartifact.exception.HttpErrorInternalServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                "FORBIDDEN",
                e.getMessage()
        );
    }
}
