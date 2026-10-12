package com.example.inventory.error;

import java.time.Instant;
import java.util.Map;

/** The one error shape every failure returns. fieldErrors is empty unless validation failed. */
public record ApiError(Instant timestamp, int status, String error, String message, Map<String, String> fieldErrors) {
}
