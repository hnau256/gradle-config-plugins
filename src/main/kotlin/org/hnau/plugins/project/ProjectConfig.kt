package org.hnau.plugins.project

import org.gradle.api.Project
import org.hnau.plugins.utils.SharedConfig
import org.hnau.plugins.utils.versions.ArtifactId
import org.hnau.plugins.utils.versions.GroupId
import org.hnau.plugins.utils.versions.LibraryId

data class ProjectConfig(
    val groupId: GroupId,
    val artifactId: ArtifactId,
    val publish: Publish?,
) {

    data class Publish(
        val gitUrl: String,
        val version: String,
        val description: String,
        val developerName: String,
        val developerEmail: String,
        val licenseName: String,
        val licenseUrl: String,
    )
}

fun SharedConfig.toProjectConfig(
    project: Project,
): ProjectConfig {

    val artifactId = project
        .path
        .drop(1)
        .replace(':', '-')
        .let(::ArtifactId)

    return ProjectConfig(
        groupId = groupId,
        artifactId = artifactId,
        publish = publish?.run {
            ProjectConfig.Publish(
                gitUrl = gitUrl,
                version = version,
                description = description ?: artifactId.artifactId,
                developerName = developerName,
                developerEmail = developerEmail,
                licenseName = licenseName,
                licenseUrl = licenseUrl,
            )
        }
    )
}

val ProjectConfig.androidNamespace: String
    get() = listOf(
        groupId.groupId,
        artifactId.artifactId.replace('-', '.')
    ).joinToString(
        separator = ".",
    )