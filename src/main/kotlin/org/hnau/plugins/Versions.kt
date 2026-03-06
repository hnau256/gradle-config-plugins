package org.hnau.plugins

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Single source of truth for all dependency/plugin versions and Maven coordinates.
 * When bumping [kotlin], also update `kotlin` in gradle/libs.versions.toml.
 */
internal object Versions {
    // Bump together with `kotlin` in gradle/libs.versions.toml
    const val kotlin = "2.3.10"

    // Bump together with `agp` in gradle/libs.versions.toml
    const val agp = "9.1.0"
    const val ksp = "2.3.6"
    const val composeMultiplatform = "1.10.2"

    // Bump together with `vanniktech` in gradle/libs.versions.toml
    const val vanniktech = "0.36.0"

    // Bump together with `dokka` in gradle/libs.versions.toml
    const val dokka = "2.1.0"

    // Android SDK
    const val compileSdk = 36
    const val minSdk = 21

    // JVM bytecode — single declaration, used for toolchain and compilerOptions
    const val jvmTargetInt = 17
    val jvmTarget = JvmTarget.fromTarget(jvmTargetInt.toString())

    // ── Artifact descriptors ────────────────────────────────────────────────

    object HnauCommons {
        const val group = "org.hnau.commons"
        const val version = "1.2.2"

        fun dep(artifact: String) = "$group:$artifact:$version"

        const val kotlin = "kotlin"
        const val appModel = "app-model"
        const val appProjector = "app-projector"

        object Gen {
            const val pipeAnnotations = "gen.pipe.annotations"
            const val pipeProcessor = "gen.pipe.processor"
            const val sealUpAnnotations = "gen.sealup.annotations"
            const val sealUpProcessor = "gen.sealup.processor"
            const val enumValuesAnnotations = "gen.enumvalues.annotations"
            const val enumValuesProcessor = "gen.enumvalues.processor"
            const val loggableAnnotations = "gen.loggable.annotations"
            const val loggableProcessor = "gen.loggable.processor"
        }
    }

    object Kotlinx {
        const val serializationVersion = "1.10.0"

        fun serializationCore() = "org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion"

        fun serializationJson() = "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion"
    }

    object Arrow {
        const val version = "2.2.2"

        fun opticsKspPlugin() = "io.arrow-kt:arrow-optics-ksp-plugin:$version"
    }
}
