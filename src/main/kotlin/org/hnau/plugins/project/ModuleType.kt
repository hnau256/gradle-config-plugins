package org.hnau.plugins.project

internal sealed interface ModuleType {
    data object Jvm : ModuleType

    class Kmp(
        val compose: Boolean,
        val app: Boolean,
    ) : ModuleType
}
