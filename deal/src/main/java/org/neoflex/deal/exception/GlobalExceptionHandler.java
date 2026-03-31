package org.neoflex.deal.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.dto.response.HttpErrorInternalServiceResponse;
import org.neoflex.deal.dto.response.HttpErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<HttpErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "API_NOT_FOUND",
                "Запрашиваемый API не найден: " + e.getResourcePath()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<HttpErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException e) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "ENTITY_NOT_FOUND",
                e.getMessage()
        );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,IllegalArgumentException.class})
    public ResponseEntity<HttpErrorResponse> handlerHttpMessageNotReadableException (Exception e) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.format("Ошибка в формате запроса: %s", e.getMessage())
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<HttpErrorResponse> handlerIllegalArgumentException (Exception e) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<HttpErrorResponse> handlerMethodArgumentNotValidException (Exception e) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                getErrorMessageFromMethodArgumentNotValidException(e)
        );
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleInternalServiceException(InternalServiceException e) {
        return buildErrorInternalServiceResponse(
                "Ошибка интеграции с внешним сервисом",
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getServiceName(),
                e.getMessage(),
                e.getHttpErrorResponse()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                message
        );
    }

    private ResponseEntity<HttpErrorInternalServiceResponse> buildErrorInternalServiceResponse(
            String header,HttpStatus externalStatus, String type, String serviceName,String message,HttpErrorResponse httpErrorResponse) {
        log.error("{}: {}", header, header);
        HttpErrorInternalServiceResponse response = new HttpErrorInternalServiceResponse(
                header,
                externalStatus.value(),
                type,
                LocalDateTime.now(),
                new HttpErrorInternalServiceResponse.ServiceErrorMessage(serviceName, message,httpErrorResponse)
        );
        return ResponseEntity.status(externalStatus).body(response);
    }

    private ResponseEntity<HttpErrorResponse> buildErrorResponse(
            HttpStatus status, String type, String message) {
        log.error("{}: {}", type, message);
        HttpErrorResponse response = new HttpErrorResponse(
                status.value(),
                type,
                LocalDateTime.now(),
                message
        );
        return ResponseEntity.status(status).body(response);
    }

    private String getErrorMessageFromMethodArgumentNotValidException(Exception e){
        if (e instanceof MethodArgumentNotValidException ex) {
            return ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> String.format("Поле '%s': %s",
                            error.getField(),
                            error.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
        }
        return e.getMessage();
    }
}
