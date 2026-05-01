package org.neoflex.calculator.exception;

import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.exception.BaseGlobalExceptionHandler;
import org.neoflex.creditapplicationsupportstarter.exception.HttpErrorInternalServiceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @Override
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

    @Override
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
}
