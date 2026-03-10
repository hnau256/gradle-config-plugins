package org.hnau.plugins.utils

import org.gradle.api.Project
import org.hnau.plugins.utils.versions.GroupId

data class SharedConfig(
    val groupId: GroupId,
    val publish: Publish?,
) {

    data class Publish(
        val gitUrl: String,
        val version: String,
        val description: String?,
        val developerName: String,
        val developerEmail: String,
        val licenseName: String,
        val licenseUrl: String,
    )

    open class Wrapper(
        val config: SharedConfig,
    )

    fun publishToRootProject(
        rootProject: Project,
    ) {
        rootProject
            .extensions
            .create(
                "hnauShardConfig",
                Wrapper::class.java,
                this,
            )
    }

    companion object {

        fun extractFromRootProject(
            project: Project,
        ): SharedConfig = project
            .rootProject
            .extensions
            .findByType(Wrapper::class.java)!!
            .config
    }
}