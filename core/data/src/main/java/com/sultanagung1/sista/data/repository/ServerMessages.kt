package com.sultanagung1.sista.data.repository

import com.google.gson.JsonParser

/**
 * The `message` of a Laravel JSON error body ("Anda tidak berwenang memberikan
 * persetujuan pada langkah ini."), so the screen can say why instead of a
 * status code. Null when the body has none.
 */
internal fun serverMessageOf(errorBody: String?): String? = errorBody
    ?.takeIf { it.isNotBlank() }
    ?.let { runCatching { JsonParser.parseString(it).asJsonObject }.getOrNull() }
    ?.get("message")
    ?.takeIf { it.isJsonPrimitive }
    ?.asString
    ?.takeIf { it.isNotBlank() }
