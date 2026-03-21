package org.hnau.plugins.project.projectext

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.Constants
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.InputStream

internal fun Project.applyKotlinComposePlugin() {

    applyPlugin(Versions.Plugins.kotlinCompose.withoutAlias.withoutVersion)

    val stabilityConfigPath = project
        .layout
        .buildDirectory
        .file(Constants.composeStabilityConfigBuildFileName)

    project
        .extensions
        .getByType<ComposeCompilerGradlePluginExtension>()
        .apply { stabilityConfigurationFiles.add(stabilityConfigPath) }

    project
        .tasks
        .register(Constants.copyHnauComposeStabilityConfigTaskName) { task ->

            task.doFirst {

                val configContent = project
                    .buildscript
                    .classLoader
                    .getResourceAsStream(Constants.composeStabilityConfigResourcesFileName)
                    ?.use(InputStream::readBytes)
                    ?: throw IllegalStateException("${Constants.composeStabilityConfigResourcesFileName} not found in plugin resources")

                stabilityConfigPath
                    .get()
                    .asFile
                    .apply { parentFile.mkdirs() }
                    .writeBytes(configContent)
            }

            task
                .outputs
                .file(stabilityConfigPath)
        }

    project
        .tasks
        .withType(KotlinCompile::class.java)
        .configureEach { task ->
            task.dependsOn(Constants.copyHnauComposeStabilityConfigTaskName)
        }
}