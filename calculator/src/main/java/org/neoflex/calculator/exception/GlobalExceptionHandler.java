package org.neoflex.calculator.exception;

import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.dto.response.HttpErrorResponse;
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

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<HttpErrorResponse> handlerHttpMessageNotReadableException (Exception e) {

        String message = "Ошибка в формате запроса: ";

        if (e.getMessage().contains("Required request body is missing")) {
            message += "Тело запроса отсутствует";
        } else {
            message += e.getMessage();
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message
        );
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<HttpErrorResponse> handlerIllegalArgumentException (Exception e) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<HttpErrorResponse> handlerMethodArgumentNotValidException (Exception e) {

        String message = getErrorMessageFromMethodArgumentNotValidException(e);
        HttpStatus status;

        if (message.contains("Отказ в займе")){
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }else {
            status = HttpStatus.BAD_REQUEST;
        }

        return buildErrorResponse(
                status,
                status.getReasonPhrase(),
                message
        );
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
