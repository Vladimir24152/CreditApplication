package org.neoflex.calculator.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.dto.response.HttpErrorInternalServiceResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
    public ResponseEntity<HttpErrorInternalServiceResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.NOT_FOUND,
                "API_NOT_FOUND",
                "Запрашиваемый API не найден: " + e.getResourcePath()
        );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerHttpMessageNotReadableException(Exception e) {

        String message = "Ошибка в формате запроса: ";

        if (e.getMessage().contains("Required request body is missing")) {
            message += "Тело запроса отсутствует";
        } else {
            message += e.getMessage();
        }

        return buildErrorInternalServiceResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerIllegalArgumentException(Exception e) {

        return buildErrorInternalServiceResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<HttpErrorInternalServiceResponse> handlerMethodArgumentNotValidException(Exception e) {

        String message = getErrorMessageFromMethodArgumentNotValidException(e);
        HttpStatus status;

        if (message.contains("Отказ в займе")){
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }else {
            status = HttpStatus.BAD_REQUEST;
        }

        return buildErrorInternalServiceResponse(
                status,
                status.getReasonPhrase(),
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

    private ResponseEntity<HttpErrorInternalServiceResponse> buildErrorInternalServiceResponse(
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

    private String getErrorMessageFromMethodArgumentNotValidException(Exception e){
        if (e instanceof MethodArgumentNotValidException ex) {
            return ex.getBindingResult()
                    .getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        }
        return e.getMessage();
    }
}
