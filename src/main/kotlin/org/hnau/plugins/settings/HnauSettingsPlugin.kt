package org.hnau.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.hnau.plugins.Versions
import java.io.File

class HnauSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension =
            settings.extensions.create(
                "hnau",
                SharedConfigExtension::class.java,
            )

        settings.dependencyResolutionManagement { management ->
            management.repositories { repos ->
                repos.google()
                repos.mavenCentral()
                repos.mavenLocal()
            }
            management.versionCatalogs { catalogs ->
                createBomCatalog(catalogs)
            }
        }

        settings.gradle.beforeProject { project ->
            if (project == project.rootProject) {
                extension
                    .toSharedConfig()
                    .publishToRootProject(project)
            }
        }

        settings.include(
            collectProjects(
                projectDir = settings.rootDir,
            ),
        )
    }

    private fun collectProjects(
        projectDir: File,
        projectIdentifier: String = "",
    ): List<String> = projectDir
        .list()
        .orEmpty()
        .let { fileNames ->
            if (projectIdentifier.isNotEmpty() && "build.gradle.kts" in fileNames) {
                return listOf(projectIdentifier)
            }
            fileNames
                .filterNot { it.startsWith('.') || it == "build" }
                .mapNotNull { fileName ->
                    val childDir = projectDir
                        .resolve(fileName)
                        .takeIf(File::isDirectory)
                        ?: return@mapNotNull null
                    fileName to childDir
                }
                .flatMap { (fileName, childDir) ->
                    collectProjects(
                        projectDir = childDir,
                        projectIdentifier = "$projectIdentifier:$fileName"
                    )
                }
        }

    private fun createBomCatalog(catalogs: MutableVersionCatalogContainer) {
        catalogs.create("hnau") { catalog ->

            val plugins =
                buildList {
                    addAll(
                        listOf(
                            // Versions.Plugins.kotlinJvm,
                            // Versions.Plugins.kotlinMultiplatform,
                            // Versions.Plugins.kotlinAndroid,
                            Versions.Plugins.kotlinSerialization,
                            // Versions.Plugins.kotlinCompose,
                            // Versions.Plugins.androidApplication,
                            // Versions.Plugins.androidMultiplatformLibrary,
                            // Versions.Plugins.composeMultiplatform,
                            Versions.Plugins.ksp,
                            // Versions.Plugins.vanniktech,
                            // Versions.Plugins.dokka,
                            // Versions.Plugins.googleServices,
                        ),
                    )
                    addAll(
                        Versions.Plugins.hnauProject,
                    )
                }

            val libraries = buildList {
                addAll(Versions.HnauCommons.forBom)
                addAll(Versions.Kotlinx.forBom)
                addAll(Versions.Standalone.forBom)
            }

            val usedVersions = listOf(
                plugins,
                libraries,
            )
                .flatten()
                .map { it.withoutAlias.version }

            usedVersions.forEach { version ->
                catalog.version(
                    version.alias.alias,
                    version.version,
                )
            }

            plugins.forEach { aliasedVersionedPluginId ->
                val (pluginIdWithVersion, alias) = aliasedVersionedPluginId
                val (pluginId, version) = pluginIdWithVersion
                catalog.plugin(alias.alias, pluginId.id).versionRef(version.alias.alias)
            }

            libraries.forEach { aliasedVersionedLibraryId ->
                val (libraryIdWithVersion, alias) = aliasedVersionedLibraryId
                val (libraryId, version) = libraryIdWithVersion
                val (groupId, artifactId) = libraryId
                catalog.library(alias.alias, groupId.groupId, artifactId.artifactId).versionRef(version.alias.alias)
            }
        }
    }
}
