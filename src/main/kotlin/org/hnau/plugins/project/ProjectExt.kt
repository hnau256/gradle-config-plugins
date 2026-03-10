package org.hnau.plugins.project

import org.gradle.api.Project
import org.gradle.internal.serialize.codecs.core.NodeOwner
import org.gradle.kotlin.dsl.dependencies
import org.hnau.plugins.utils.versions.LibraryId
import org.hnau.plugins.utils.versions.PluginId
import org.hnau.plugins.utils.versions.Versioned

internal fun Project.applyPlugin(
    plugin: PluginId,
) {
    plugins.apply(
        plugin.id,
    )
}

fun Project.addDependency(
    type: ProjectType,
    dependency: Versioned<LibraryId>,
) {
    when (type) {
        ProjectType.Jvm -> addDependency("implementation", dependency)
        is ProjectType.Kmp -> type
            .commonMainSourceSet
            .configure { commonMainSourceSet ->
                commonMainSourceSet.dependencies {
                    implementation(dependency.asDependency)
                }
            }
    }
}

fun Project.addDependency(
    configurationName: String,
    dependency: Versioned<LibraryId>,
) {
    dependencies {
        add(
            configurationName,
            dependency.asDependency,
        )
    }
}

private val Versioned<LibraryId>.asDependency: String
    get() = listOf(
        withoutVersion.groupId.groupId,
        withoutVersion.artifactId.artifactId,
        version.version
    ).joinToString(
        separator = ":",
    )

fun Project.hasPlugin(
    plugin: PluginId,
): Boolean = plugins.hasPlugin(
    plugin.id,
)