package org.neoflex.calculator.dto.response;

import java.time.LocalDateTime;

public record HttpErrorResponse(int code, String type, LocalDateTime timestamp, String message) {
}
