package org.hnau.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.Kotlinx

/**
 * Settings plugin that:
 * 1. Creates hnau.versions.toml BOM catalog with plugins and libraries
 * 2. Stores configuration in rootProject.extensions for module plugins to access
 */
class HnauSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension =
            settings.extensions.create(
                "hnau",
                HnauSettingsExtension::class.java,
                settings,
            )

        // Configure repositories and create catalog
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

        // Create settings container in root project for module plugins to access
        settings.gradle.beforeProject { project ->
            if (project == project.rootProject) {
                project.extensions.create("hnauSettings", HnauSettingsContainer::class.java).apply {
                    includeCommonsKotlinDependency = extension.includeCommonsKotlinDependency
                    publishSettings = extension.publishSettings
                }
            }
        }
    }

    private fun createBomCatalog(catalogs: MutableVersionCatalogContainer) {
        catalogs.create("hnau") { catalog ->
            // ── Versions ───────────────────────────────────────────────────────
            val kotlinVersion = catalog.version("kotlin", Versions.kotlin)
            val agpVersion = catalog.version("agp", Versions.agp)
            val kspVersion = catalog.version("ksp", Versions.ksp)
            val composeVersion = catalog.version("compose", Versions.composeMultiplatform)
            val hnauCommonsVersion = catalog.version("hnau-commons", HnauCommons.version)

            // ── Plugins ────────────────────────────────────────────────────────
            catalog.plugin("kotlin-jvm", Versions.PluginIds.kotlinJvm).versionRef(kotlinVersion)
            catalog.plugin("kotlin-multiplatform", Versions.PluginIds.kotlinMultiplatform).versionRef(kotlinVersion)
            catalog.plugin("kotlin-android", Versions.PluginIds.kotlinAndroid).versionRef(kotlinVersion)
            catalog.plugin("kotlin-serialization", Versions.PluginIds.kotlinSerialization).versionRef(kotlinVersion)
            catalog.plugin("kotlin-compose", Versions.PluginIds.kotlinCompose).versionRef(kotlinVersion)
            catalog.plugin("android-library", Versions.PluginIds.androidLibrary).versionRef(agpVersion)
            catalog.plugin("android-application", Versions.PluginIds.androidApplication).versionRef(agpVersion)
            catalog.plugin("compose-multiplatform", Versions.PluginIds.composeMultiplatform).versionRef(composeVersion)
            catalog.plugin("ksp", Versions.PluginIds.ksp).versionRef(kspVersion)
            catalog.plugin("vanniktech", Versions.PluginIds.vanniktech).version(Versions.vanniktech)
            catalog.plugin("dokka", Versions.PluginIds.dokka).version(Versions.dokka)

            // ── Libraries ──────────────────────────────────────────────────────
            catalog.library("commons-app-model", HnauCommons.group, HnauCommons.appModel).versionRef(hnauCommonsVersion)
            catalog.library("commons-app-projector", HnauCommons.group, HnauCommons.appProjector).versionRef(hnauCommonsVersion)
            catalog
                .library(
                    "kotlinx-serialization-json",
                    Kotlinx.group,
                    Kotlinx.serializationJsonArtifact,
                ).version(Kotlinx.serializationVersion)
        }
    }
}

/**
 * Container class for all hnau settings.
 * Stored in rootProject.extensions["hnauSettings"] for module plugins to access.
 */
open class HnauSettingsContainer {
    var includeCommonsKotlinDependency: Boolean = true
    var publishSettings: PublishSettings? = null
}
