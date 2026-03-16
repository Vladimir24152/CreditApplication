package org.neoflex.calculator.exception;

import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.dto.response.HttpErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotValidBirthDateException.class})
    public ResponseEntity<HttpErrorResponse> handlerInternalServerErrorException(Exception e) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({ScoringFailedException.class})
    public ResponseEntity<HttpErrorResponse> handlerScrollingFailedErrorException(Exception e) {
        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                e.getMessage()
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<HttpErrorResponse> handlerMethodArgumentNotValidException (Exception e) {
        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                getErrorMessageFromMethodArgumentNotValidException(e)
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
