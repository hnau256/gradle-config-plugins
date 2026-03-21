package org.hnau.plugins.project.projectext

import org.gradle.api.Project
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.ProjectConfig
import org.hnau.plugins.project.utils.ProjectType

internal fun Project.configureCommon(
    config: ProjectConfig,
    projectType: ProjectType,
) {

    if (config.groupId != Versions.HnauCommons.group) {
        addDependency(
            type = projectType,
            dependency = Versions.HnauCommons.kotlin,
        )
    }

    buildList {
        addAll(Versions.Arrow.unconditioned)
        addAll(Versions.Kotlinx.unconditioned)
        addAll(Versions.Standalone.unconditioned)
    }.forEach { arrowDependency ->
        addDependency(
            type = projectType,
            dependency = arrowDependency,
        )
    }

    addTestDependency(
        type = projectType,
        dependency = Versions.kotlinTest,
    )

    configureSerializationIfNeed(
        projectType = projectType,
    )

    val hasKsp = hasPlugin(Versions.Plugins.ksp.withoutAlias.withoutVersion)
    if (hasKsp) {
        configureKsp(
            projectType = projectType,
        )
    }

    config.publish?.let { publish ->
        configurePublishing(
            publish = publish,
            projectConfig = config,
            projectType = projectType,
            hasKsp = hasKsp,
        )
    }
}
