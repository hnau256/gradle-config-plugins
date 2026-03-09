package org.hnau.plugins.project.entrypoints

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.hnau.plugins.project.ModuleConfigurator
import org.hnau.plugins.project.ModuleType

/**
 * Entry point plugin for Android Application modules.
 * Applies: com.android.application, kotlin-android, compose-compiler, maven-publish
 */
class HnauAndroidAppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        ModuleConfigurator.configure(project, ModuleType.ANDROID_APP)
    }
}
