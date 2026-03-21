package org.hnau.plugins.project.projectext

import org.gradle.api.Project
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.ProjectType

internal fun Project.configureSerializationIfNeed(
    projectType: ProjectType,
) {
    if (!hasPlugin(Versions.Plugins.kotlinSerialization.withoutAlias.withoutVersion)) {
        return
    }

    Versions
        .Kotlinx
        .serialization
        .forEach { dependency ->
            addDependency(
                type = projectType,
                dependency = dependency,
            )
        }
}