package org.hnau.plugins.project.entrypoints

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hnau.plugins.project.ModuleConfigurator
import org.hnau.plugins.project.ModuleType

/**
 * Entry point plugin for Kotlin JVM modules.
 * Applies: kotlin-jvm, maven-publish
 */
class HnauJvmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        ModuleConfigurator.configure(project, ModuleType.JVM)
    }
}
