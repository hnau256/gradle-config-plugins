package org.hnau.plugins.utils

internal fun <T : Any> requireNotNull(
    value: T?,
    propertyName: String,
): T = requireNotNull(value) {
    "Expected '$propertyName', got null"
}