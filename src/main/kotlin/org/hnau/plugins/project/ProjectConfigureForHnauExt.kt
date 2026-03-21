package org.hnau.plugins.project

import org.gradle.api.Project
import org.hnau.plugins.project.projectext.*
import org.hnau.plugins.project.utils.ModuleType
import org.hnau.plugins.project.utils.ProjectType
import org.hnau.plugins.project.utils.toProjectConfig
import org.hnau.plugins.utils.SharedConfig

internal fun Project.configureForHnau(
    moduleType: ModuleType,
) {
    val config = SharedConfig
        .extractFromRootProject(project)
        .toProjectConfig(project)

    val projectType: ProjectType = when (moduleType) {
        ModuleType.JVM -> configureJvm(
            config = config,
            addAndroid = false,
        )

        ModuleType.KMP -> configureKmp(
            config = config,
            addCompose = false,
        )

        ModuleType.UI -> configureKmp(
            config = config,
            addCompose = true,
        )

        ModuleType.ANDROID_APP -> configureJvm(
            config = config,
            addAndroid = true,
        )
    }

    configureCommon(
        config = config,
        projectType = projectType,
    )
}