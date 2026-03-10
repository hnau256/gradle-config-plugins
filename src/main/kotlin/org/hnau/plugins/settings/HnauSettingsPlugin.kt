package org.hnau.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.hnau.plugins.Versions

class HnauSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {

        val extension = settings.extensions.create(
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
    }

    private fun createBomCatalog(catalogs: MutableVersionCatalogContainer) {

        catalogs.create("hnau") { catalog ->

            val plugins = buildList {
                addAll(
                    listOf(
                        //Versions.Plugins.kotlinJvm,
                        //Versions.Plugins.kotlinMultiplatform,
                        //Versions.Plugins.kotlinAndroid,
                        Versions.Plugins.kotlinSerialization,
                        //Versions.Plugins.kotlinCompose,
                        //Versions.Plugins.androidApplication,
                        //Versions.Plugins.androidMultiplatformLibrary,
                        //Versions.Plugins.composeMultiplatform,
                        Versions.Plugins.ksp,
                        //Versions.Plugins.vanniktech,
                        //Versions.Plugins.dokka,
                        //Versions.Plugins.googleServices,
                    )
                )
                addAll(
                    Versions.Plugins.hnauProject
                )
            }

            val libraries = listOf(
                Versions.HnauCommons.appModel,
                Versions.HnauCommons.appProjector,
                Versions.Kotlinx.immutable,
            )

            val usedVersions = listOf(
                plugins,
                libraries
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
