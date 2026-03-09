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

        // Propagate settings to all projects via beforeProject hook
        settings.gradle.beforeProject { project ->
            project.extensions.extraProperties["hnau.includeCommonsKotlinDependency"] = extension.includeCommonsKotlinDependency
            extension.publishSettings?.let {
                project.extensions.extraProperties["hnau.publish.groupId"] = it.groupId
                project.extensions.extraProperties["hnau.publish.gitUrl"] = it.gitUrl
                it.artifactId?.let { artifactId -> project.extensions.extraProperties["hnau.publish.artifactId"] = artifactId }
                it.version?.let { version -> project.extensions.extraProperties["hnau.publish.version"] = version }
                it.description?.let { desc -> project.extensions.extraProperties["hnau.publish.description"] = desc }
                it.developerName?.let { name -> project.extensions.extraProperties["hnau.publish.developerName"] = name }
                it.developerEmail?.let { email -> project.extensions.extraProperties["hnau.publish.developerEmail"] = email }
                it.licenseName?.let { license -> project.extensions.extraProperties["hnau.publish.licenseName"] = license }
                it.licenseUrl?.let { url -> project.extensions.extraProperties["hnau.publish.licenseUrl"] = url }
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
