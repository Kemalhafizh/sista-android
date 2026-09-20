package com.sultanagung1.sista.core.telemetry

import java.util.regex.Pattern

/**
 * Sanitizer data telemetri untuk mencegah kebocoran PII (Personally Identifiable Information)
 * dan kredensial rahasia (Bearer token, password, NISN, NIK, PIN, Cookie).
 */
object TelemetrySanitizer {

    private val SENSITIVE_KEYS = setOf(
        "authorization", "cookie", "set-cookie", "x-csrf-token",
        "password", "secret", "token", "access_token", "refresh_token",
        "pin", "nik", "nisn", "api_key", "credential"
    )

    private val SENSITIVE_QUERY_PARAM_REGEX = Pattern.compile(
        "(?i)(password|token|access_token|refresh_token|secret|pin|nik|nisn|api_key|credential)=([^&]+)"
    )

    private val BEARER_TOKEN_REGEX = Pattern.compile(
        "(?i)Bearer\\s+([a-zA-Z0-9_\\-\\.]+)"
    )

    fun sanitizeUrl(url: String): String {
        var sanitized = SENSITIVE_QUERY_PARAM_REGEX.matcher(url).replaceAll("$1=[REDACTED]")
        sanitized = BEARER_TOKEN_REGEX.matcher(sanitized).replaceAll("Bearer [REDACTED]")
        return sanitized
    }

    fun sanitizeHeaders(headers: Map<String, String>): Map<String, String> {
        return headers.mapValues { (key, value) ->
            if (isSensitiveKey(key)) {
                "[REDACTED]"
            } else {
                sanitizeString(value)
            }
        }
    }

    fun sanitizeData(data: Map<String, String>): Map<String, String> {
        return data.mapValues { (key, value) ->
            if (isSensitiveKey(key)) {
                "[REDACTED]"
            } else {
                sanitizeString(value)
            }
        }
    }

    fun sanitizeString(input: String): String {
        var result = BEARER_TOKEN_REGEX.matcher(input).replaceAll("Bearer [REDACTED]")
        result = SENSITIVE_QUERY_PARAM_REGEX.matcher(result).replaceAll("$1=[REDACTED]")
        return result
    }

    fun isSensitiveKey(key: String): Boolean {
        val lower = key.lowercase()
        return SENSITIVE_KEYS.any { lower.contains(it) }
    }
}
