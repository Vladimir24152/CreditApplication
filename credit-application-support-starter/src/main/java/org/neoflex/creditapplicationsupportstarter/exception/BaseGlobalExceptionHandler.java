package org.neoflex.creditapplicationsupportstarter.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
public class BaseGlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.NOT_FOUND,
                "API_NOT_FOUND",
                "Запрашиваемый API не найден: " + e.getResourcePath()
        );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerHttpMessageNotReadableException(Exception e) {
        return buildErrorInternalServiceResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.format("Ошибка в формате запроса: %s", e.getMessage())
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerIllegalStateException(
            IllegalStateException e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.CONFLICT,
                "ILLEGAL_STATE",
                e.getMessage()
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerNullPointerException(Exception e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerMethodArgumentNotValidException(Exception e) {
        return buildErrorInternalServiceResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                getErrorMessageFromMethodArgumentNotValidException(e)
        );
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleInternalServiceException(InternalServiceException e) {
        return buildErrorInternalServiceResponse(
                "Ошибка интеграции с внешним сервисом",
                HttpStatus.BAD_GATEWAY,
                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                e.getServiceName(),
                e.getMessage(),
                e.getHttpErrorInternalServiceResponse()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        return buildErrorInternalServiceResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                message
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpErrorInternalServiceResponse> handleRuntimeException(RuntimeException e) {
        return buildErrorInternalServiceResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Произошла внутренняя ошибка сервера"
        );
    }

    protected ResponseEntity<HttpErrorInternalServiceResponse> buildErrorInternalServiceResponse(
            String message, HttpStatus externalStatus, String type, String serviceName, String messageInnerService, HttpErrorInternalServiceResponse httpErrorResponse) {
        log.error("{}: {}", type, message);
        HttpErrorInternalServiceResponse response = new HttpErrorInternalServiceResponse(
                externalStatus.value(),
                type,
                LocalDateTime.now(),
                message,
                new HttpErrorInternalServiceResponse.ServiceErrorMessage(serviceName, messageInnerService, httpErrorResponse)
        );
        return ResponseEntity.status(externalStatus).body(response);
    }

    protected ResponseEntity<HttpErrorInternalServiceResponse> buildErrorInternalServiceResponse(
            HttpStatus externalStatus, String type, String message) {
        log.error("{}: {}", type, message);
        HttpErrorInternalServiceResponse response = new HttpErrorInternalServiceResponse(
                externalStatus.value(),
                type,
                LocalDateTime.now(),
                message,
                null
        );
        return ResponseEntity.status(externalStatus).body(response);
    }

    protected String getErrorMessageFromMethodArgumentNotValidException(Exception e) {
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
