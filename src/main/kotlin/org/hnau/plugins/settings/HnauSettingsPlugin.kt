package org.hnau.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.Arrow
import org.hnau.plugins.Versions.Compose
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.Kotlinx
import org.hnau.plugins.Versions.PluginIds
import java.io.File

class HnauSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val extension =
            settings.extensions.create(
                "hnauSettings",
                HnauSettingsExtension::class.java,
                settings,
            )

        settings.pluginManagement { spec ->
            spec.repositories { repos ->
                repos.gradlePluginPortal()
                repos.google()
                repos.mavenCentral()
                repos.mavenLocal()
            }
            spec.plugins { plugins ->
                plugins.id(PluginIds.kotlinMultiplatform).version(Versions.kotlin)
                plugins.id(PluginIds.kotlinJvm).version(Versions.kotlin)
                plugins.id(PluginIds.kotlinSerialization).version(Versions.kotlin)
                plugins.id(PluginIds.kotlinCompose).version(Versions.kotlin)
                plugins.id(PluginIds.androidKmpLibrary).version(Versions.agp)
                plugins.id(PluginIds.androidLibrary).version(Versions.agp)
                plugins.id(PluginIds.ksp).version(Versions.ksp)
                plugins.id(PluginIds.composeMultiplatform).version(Versions.composeMultiplatform)
                plugins.id(PluginIds.dokka).version(Versions.dokka)
            }
        }

        settings.dependencyResolutionManagement { management ->
            management.repositories { repos ->
                repos.google()
                repos.mavenCentral()
                repos.mavenLocal()
            }
            management.versionCatalogs { catalogs ->
                buildHnauCatalog(catalogs)
            }
        }

        // Auto-include modules after settings are evaluated (extension block executed).
        settings.gradle.settingsEvaluated {
            if (extension.autoIncludeModules) {
                autoIncludeModules(settings)
            }
        }

        // Propagate allModules defaults to every project.
        settings.gradle.beforeProject { project ->
            val allModules = extension.allModules
            allModules.group?.let { project.extensions.extraProperties["hnau.group"] = it }
            allModules.version?.let { project.version = it }
            project.extensions.extraProperties["hnau.includeHnauCommons"] = allModules.includeHnauCommons
            allModules.gitUrl?.let { project.extensions.extraProperties["hnau.gitUrl"] = it }
        }
    }

    private fun autoIncludeModules(settings: Settings) {
        findAndIncludeModules(settings, settings.rootDir)
    }

    private fun findAndIncludeModules(
        settings: Settings,
        dir: File,
        pathPrefix: String = "",
    ) {
        dir
            .listFiles { file ->
                file.isDirectory &&
                    !file.name.startsWith(".") &&
                    file.name !in setOf("build", "gradle", "buildSrc")
            }?.forEach { file ->
                val currentPath =
                    listOfNotNull(
                        pathPrefix.takeIf { it.isNotEmpty() },
                        file.name,
                    ).joinToString(separator = ":")
                when {
                    file.resolve("build.gradle.kts").exists() -> settings.include(":$currentPath")
                    else -> findAndIncludeModules(settings, file, currentPath)
                }
            }
    }

    private fun buildHnauCatalog(catalogs: MutableVersionCatalogContainer) {
        catalogs.create("hnauLibs") { catalog ->
            // ── Plugins ────────────────────────────────────────────────────────
            catalog
                .plugin("kotlin-multiplatform", PluginIds.kotlinMultiplatform)
                .version(Versions.kotlin)
            catalog
                .plugin("kotlin-jvm", PluginIds.kotlinJvm)
                .version(Versions.kotlin)
            catalog
                .plugin("kotlin-serialization", PluginIds.kotlinSerialization)
                .version(Versions.kotlin)
            catalog
                .plugin("android-library", PluginIds.androidLibrary)
                .version(Versions.agp)
            catalog
                .plugin("ksp", PluginIds.ksp)
                .version(Versions.ksp)
            catalog
                .plugin("compose-multiplatform", PluginIds.composeMultiplatform)
                .version(Versions.composeMultiplatform)
            catalog
                .plugin("compose-compiler", PluginIds.kotlinCompose)
                .version(Versions.kotlin)
            catalog
                .plugin("vanniktech", PluginIds.vanniktech)
                .version(Versions.vanniktech)
            catalog
                .plugin("dokka", PluginIds.dokka)
                .version(Versions.dokka)

            // ── hnau.commons ───────────────────────────────────────────────────
            val hnauVersion = catalog.version("hnau-commons", HnauCommons.version)

            fun hnauLib(
                alias: String,
                artifact: String,
            ) = catalog.library(alias, HnauCommons.group, artifact).versionRef(hnauVersion)

            hnauLib("hnau-commons-kotlin", HnauCommons.kotlin)
            hnauLib("hnau-commons-app-model", HnauCommons.appModel)
            hnauLib("hnau-commons-app-projector", HnauCommons.appProjector)
            hnauLib("hnau-commons-gen-pipe-annotations", HnauCommons.Gen.pipeAnnotations)
            hnauLib("hnau-commons-gen-pipe-processor", HnauCommons.Gen.pipeProcessor)
            hnauLib("hnau-commons-gen-sealup-annotations", HnauCommons.Gen.sealUpAnnotations)
            hnauLib("hnau-commons-gen-sealup-processor", HnauCommons.Gen.sealUpProcessor)
            hnauLib("hnau-commons-gen-enumvalues-annotations", HnauCommons.Gen.enumValuesAnnotations)
            hnauLib("hnau-commons-gen-enumvalues-processor", HnauCommons.Gen.enumValuesProcessor)
            hnauLib("hnau-commons-gen-loggable-annotations", HnauCommons.Gen.loggableAnnotations)
            hnauLib("hnau-commons-gen-loggable-processor", HnauCommons.Gen.loggableProcessor)

            // ── kotlinx ────────────────────────────────────────────────────────
            catalog
                .library("kotlinx-serialization-core", Kotlinx.group, Kotlinx.serializationCoreArtifact)
                .version(Kotlinx.serializationVersion)
            catalog
                .library("kotlinx-serialization-json", Kotlinx.group, Kotlinx.serializationJsonArtifact)
                .version(Kotlinx.serializationVersion)

            // ── Arrow ──────────────────────────────────────────────────────────
            catalog.library("arrow-core", Arrow.group, Arrow.coreArtifact).version(Arrow.version)
            catalog.library("arrow-optics", Arrow.group, Arrow.opticsArtifact).version(Arrow.version)
            catalog.library("arrow-optics-ksp-plugin", Arrow.group, Arrow.opticsKspPluginArtifact).version(Arrow.version)

            // ── Compose ────────────────────────────────────────────────────────
            val composeVersion = catalog.version("compose-multiplatform", Versions.composeMultiplatform)
            catalog.library("compose-runtime", Compose.runtimeGroup, Compose.runtimeArtifact).versionRef(composeVersion)
            catalog.library("compose-foundation", Compose.foundationGroup, Compose.foundationArtifact).versionRef(composeVersion)
            catalog.library("compose-ui", Compose.uiGroup, Compose.uiArtifact).versionRef(composeVersion)
            catalog.library("compose-material3", Compose.material3Group, Compose.material3Artifact).version(Compose.material3Version)
            catalog.library("compose-icons-core", Compose.iconsCoreGroup, Compose.iconsCoreArtifact).version(Compose.iconsCoreVersion)
        }
    }
}
