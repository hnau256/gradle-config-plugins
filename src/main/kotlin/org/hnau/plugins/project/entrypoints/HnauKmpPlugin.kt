package org.hnau.plugins.project.entrypoints

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hnau.plugins.project.ModuleConfigurator
import org.hnau.plugins.project.ModuleType

/**
 * Entry point plugin for Kotlin Multiplatform modules.
 * Applies: kotlin-multiplatform, android-kmp-library, maven-publish
 */
class HnauKmpPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        ModuleConfigurator.configure(project, ModuleType.KMP)
    }
}
