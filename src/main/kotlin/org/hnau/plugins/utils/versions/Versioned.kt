package org.hnau.plugins.utils.versions

data class Versioned<out T>(
    val withoutVersion: T,
    val version: Version,
)

infix fun <T> T.withVersion(
    version: Version,
): Versioned<T> = Versioned(
    withoutVersion = this,
    version = version,
)