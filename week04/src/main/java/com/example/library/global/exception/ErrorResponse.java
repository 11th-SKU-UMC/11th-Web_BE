package com.example.library.global.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(LocalDateTime timestamp, String code, String message,
                            Map<String, String> details) {
}
