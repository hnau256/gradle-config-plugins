package org.hnau.plugins.project.projectext

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.Constants
import org.hnau.plugins.project.utils.ProjectConfig
import org.hnau.plugins.project.utils.ProjectType
import org.hnau.plugins.project.utils.androidNamespace
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKmp(
    config: ProjectConfig,
    addCompose: Boolean,
): ProjectType.Kmp {
    applyPlugin(Versions.Plugins.kotlinMultiplatform.withoutAlias.withoutVersion)
    applyPlugin(Versions.Plugins.androidMultiplatformLibrary.withoutAlias.withoutVersion)

    project
        .extensions
        .getByType(KotlinMultiplatformExtension::class.java)
        .jvmToolchain(Versions.jvmTargetInt)

    if (addCompose) {
        applyPlugin(Versions.Plugins.composeMultiplatform.withoutAlias.withoutVersion)
        applyKotlinComposePlugin()
    }

    val projectType = ProjectType.Kmp(
        kmpExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
    )

    addAndroidDependencies(
        projectType = projectType,
        addCompose = addCompose,
    )

    projectType.kmpExtension.compilerOptions {
        freeCompilerArgs.addAll(Constants.kotlinFreeCompilerArgs)
    }

    (projectType.kmpExtension as ExtensionAware)
        .extensions
        .getByType(KotlinMultiplatformAndroidLibraryExtension::class.java)
        .apply {
            namespace = config.androidNamespace
            compileSdk = Versions.compileSdk
            minSdk = Versions.minSdk
        }

    when (addCompose) {
        true -> {
            projectType
                .kmpExtension
                .jvm(Constants.desktopTargetName) {
                    withSourcesJar()
                }

            dependencies.add(
                "${Constants.desktopTargetName}MainImplementation",
                ComposePlugin.Dependencies(project).desktop.currentOs,
            )

            addComposeDependencies(
                dependencies = Versions.composeMultiplatform,
                projectType = projectType,
            )
        }

        false -> {

            projectType
                .kmpExtension
                .jvm {
                    withSourcesJar()
                }

            projectType
                .kmpExtension
                .linuxX64()
        }
    }

    return projectType
}