package org.hnau.plugins.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.MutableVersionCatalogContainer
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.Arrow
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.Kotlinx
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
            allModules.group?.let { project.group = it }
            allModules.version?.let { project.version = it }
            project.extensions.extraProperties["hnau.includeHnauCommons"] =
                allModules.includeHnauCommons
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
        catalogs.create("hnau") { catalog ->
            // ── Plugins ────────────────────────────────────────────────────────
            catalog
                .plugin("kotlin-multiplatform", "org.jetbrains.kotlin.multiplatform")
                .version(Versions.kotlin)
            catalog
                .plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm")
                .version(Versions.kotlin)
            catalog
                .plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization")
                .version(Versions.kotlin)
            catalog
                .plugin("android-library", "com.android.library")
                .version(Versions.agp)
            catalog
                .plugin("ksp", "com.google.devtools.ksp")
                .version(Versions.ksp)
            catalog
                .plugin("compose-multiplatform", "org.jetbrains.compose")
                .version(Versions.composeMultiplatform)
            catalog
                .plugin("compose-compiler", "org.jetbrains.kotlin.plugin.compose")
                .version(Versions.kotlin)
            catalog
                .plugin("vanniktech", "com.vanniktech.maven.publish")
                .version(Versions.vanniktech)
            catalog
                .plugin("dokka", "org.jetbrains.dokka")
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
                .library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core")
                .version(Kotlinx.serializationVersion)
            catalog
                .library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json")
                .version(Kotlinx.serializationVersion)

            // ── Arrow ──────────────────────────────────────────────────────────
            catalog.library("arrow-core", "io.arrow-kt", "arrow-core").version(Arrow.version)
            catalog.library("arrow-optics", "io.arrow-kt", "arrow-optics").version(Arrow.version)
            catalog.library("arrow-optics-ksp-plugin", "io.arrow-kt", "arrow-optics-ksp-plugin").version(Arrow.version)

            // ── Compose ────────────────────────────────────────────────────────
            val composeVersion = catalog.version("compose-multiplatform", Versions.composeMultiplatform)
            catalog.library("compose-runtime", "org.jetbrains.compose.runtime", "runtime").versionRef(composeVersion)
            catalog.library("compose-foundation", "org.jetbrains.compose.foundation", "foundation").versionRef(composeVersion)
            catalog.library("compose-ui", "org.jetbrains.compose.ui", "ui").versionRef(composeVersion)
            catalog.library("compose-material3", "org.jetbrains.compose.material3", "material3").version("1.10.0-alpha05")
            catalog.library("compose-icons-core", "org.jetbrains.compose.material", "material-icons-core").version("1.7.3")
        }
    }
}
