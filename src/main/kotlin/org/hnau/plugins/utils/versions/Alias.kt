package org.hnau.plugins.utils.versions

@JvmInline
value class Alias(
    val alias: String,
)

data class Aliased<out T>(
    val withoutAlias: T,
    val alias: Alias,
)



infix fun <T> T.withAlias(
    alias: Alias,
): Aliased<T> = Aliased(
    withoutAlias = this,
    alias = alias,
)

infix fun <T> T.withAlias(
    alias: String,
): Aliased<T> = this withAlias Alias(alias)