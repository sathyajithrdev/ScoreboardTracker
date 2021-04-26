package com.saj.android.scoreboardtracker.extensions

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


inline fun <reified T> String.deserialize(): T {
    return Json.decodeFromString(this)
}

inline fun <reified T> T.serialize(): String {
    return Json.encodeToString(this)
}

inline fun String.nullIfEmpty() = if (this.isBlank()) null else this