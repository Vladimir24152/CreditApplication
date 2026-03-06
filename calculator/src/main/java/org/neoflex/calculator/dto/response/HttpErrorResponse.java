package org.neoflex.calculator.dto.response;

public record HttpErrorResponse(int code, String type, String message) {
}
