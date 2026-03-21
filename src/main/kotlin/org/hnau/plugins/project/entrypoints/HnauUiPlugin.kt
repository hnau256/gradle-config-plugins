package org.hnau.plugins.project.entrypoints

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hnau.plugins.project.configureForHnau
import org.hnau.plugins.project.utils.ModuleType

class HnauUiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureForHnau(ModuleType.UI)
    }
}
