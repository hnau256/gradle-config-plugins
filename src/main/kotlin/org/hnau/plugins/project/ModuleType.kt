package org.hnau.plugins.project

sealed interface ModuleType {
    data object Jvm : ModuleType

    class Kmp : ModuleType {
        var compose: Boolean = false

        /**
         * Adds org.hnau.commons:app-model dependency.
         * If compose==true, also adds org.hnau.commons:app-projector.
         */
        var app: Boolean = false
    }
}
