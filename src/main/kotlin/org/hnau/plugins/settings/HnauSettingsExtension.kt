package org.hnau.plugins.settings

import org.gradle.api.Action
import org.gradle.api.initialization.Settings

open class HnauSettingsExtension(
    private val settings: Settings,
) {
    var autoIncludeModules: Boolean = true

    val allModules: AllModulesExtension = AllModulesExtension()

    fun allModules(action: Action<AllModulesExtension>) {
        action.execute(allModules)
    }
}
