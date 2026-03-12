package org.hnau.plugins.utils

fun <T : Any> requireNotNull(
    value: T?,
    propertyName: String,
): T = requireNotNull(value) {
    "Expected '$propertyName', got null"
}