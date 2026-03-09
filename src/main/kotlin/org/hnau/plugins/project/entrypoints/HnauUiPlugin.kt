package org.hnau.plugins.project.entrypoints

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hnau.plugins.project.ModuleConfigurator
import org.hnau.plugins.project.ModuleType

/**
 * Entry point plugin for UI modules with Compose Multiplatform.
 * Applies: kotlin-multiplatform, android-kmp-library, compose-multiplatform, compose-compiler, maven-publish
 */
class HnauUiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        ModuleConfigurator.configure(project, ModuleType.UI)
    }
}
