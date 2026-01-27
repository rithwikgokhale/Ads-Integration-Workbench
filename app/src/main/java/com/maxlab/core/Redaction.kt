package com.maxlab.core

fun redactId(input: String?): String? {
    if (input.isNullOrBlank()) return input
    if (input.length <= 8) return "****"
    return input.take(4) + "..." + input.takeLast(4)
}

fun sanitizePayload(payload: String?, secrets: List<String?>): String? {
    if (payload.isNullOrBlank()) return payload
    var sanitized = payload
    secrets.filterNotNull().filter { it.isNotBlank() }.forEach { secret ->
        sanitized = sanitized.replace(secret, redactId(secret) ?: "REDACTED", ignoreCase = false)
    }
    return sanitized
}
