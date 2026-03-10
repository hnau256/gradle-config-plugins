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

    // Jetpack Compose
    const val jetpackComposeVersion = "1.7.6"
    const val composeCompilerVersion = "2.3.10" // Same as Kotlin version for Kotlin 2.x

    // Bump together with `vanniktech` in gradle/libs.versions.toml
    const val vanniktech = "0.36.0"

    // Bump together with `dokka` in gradle/libs.versions.toml
    const val dokka = "2.1.0"

    // Android SDK
    const val compileSdk = 36
    const val minSdk = 23

    // JVM bytecode — single declaration, used for toolchain and compilerOptions
    const val jvmTargetInt = 17
    val jvmTarget = JvmTarget.fromTarget(jvmTargetInt.toString())

    // ── Plugin IDs ──────────────────────────────────────────────────────────

    object PluginIds {
        const val kotlinMultiplatform = "org.jetbrains.kotlin.multiplatform"
        const val kotlinJvm = "org.jetbrains.kotlin.jvm"
        const val kotlinAndroid = "org.jetbrains.kotlin.android"
        const val kotlinSerialization = "org.jetbrains.kotlin.plugin.serialization"
        const val kotlinCompose = "org.jetbrains.kotlin.plugin.compose"
        const val androidKmpLibrary = "com.android.kotlin.multiplatform.library"
        const val androidLibrary = "com.android.library"
        const val androidApplication = "com.android.application"
        const val googleServices = "com.google.gms.google-services"
        const val ksp = "com.google.devtools.ksp"
        const val composeMultiplatform = "org.jetbrains.compose"
        const val dokka = "org.jetbrains.dokka"
        const val vanniktech = "com.vanniktech.maven.publish"
        const val signing = "signing"
    }

    // ── Artifact descriptors ────────────────────────────────────────────────

    object HnauCommons {
        const val group = "org.hnau.commons"
        const val version = "1.2.4"

        fun dep(artifact: String) = "$group:$artifact:$version"

        const val kotlin = "kotlin"
        const val appModel = "app-model"
        const val appProjector = "app-projector"

        object Gen {
            const val pipeAnnotations = "gen-pipe-annotations"
            const val pipeProcessor = "gen-pipe-processor"
            const val sealUpAnnotations = "gen-sealup-annotations"
            const val sealUpProcessor = "gen-sealup-processor"
            const val enumValuesAnnotations = "gen-enumvalues-annotations"
            const val enumValuesProcessor = "gen-enumvalues-processor"
            const val loggableAnnotations = "gen-loggable-annotations"
            const val loggableProcessor = "gen-loggable-processor"
        }
    }

    object Kotlinx {
        const val group = "org.jetbrains.kotlinx"
        const val serializationCoreArtifact = "kotlinx-serialization-core"
        const val serializationJsonArtifact = "kotlinx-serialization-json"
        const val serializationCborArtifact = "kotlinx-serialization-cbor"
        const val serializationVersion = "1.10.0"

        fun serializationCore() = "$group:$serializationCoreArtifact:$serializationVersion"

        fun serializationJson() = "$group:$serializationJsonArtifact:$serializationVersion"

        fun serializationCbor() = "$group:$serializationCborArtifact:$serializationVersion"
    }

    object Arrow {
        const val group = "io.arrow-kt"
        const val opticsKspPluginArtifact = "arrow-optics-ksp-plugin"
        const val coreArtifact = "arrow-core"
        const val opticsArtifact = "arrow-optics"
        const val version = "2.2.2"

        fun opticsKspPlugin() = "$group:$opticsKspPluginArtifact:$version"
    }

    object Compose {
        const val material3Version = "1.10.0-alpha05"
        const val iconsCoreVersion = "1.7.3"

        const val runtimeGroup = "org.jetbrains.compose.runtime"
        const val runtimeArtifact = "runtime"
        const val foundationGroup = "org.jetbrains.compose.foundation"
        const val foundationArtifact = "foundation"
        const val uiGroup = "org.jetbrains.compose.ui"
        const val uiArtifact = "ui"
        const val material3Group = "org.jetbrains.compose.material3"
        const val material3Artifact = "material3"
        const val iconsCoreGroup = "org.jetbrains.compose.material"
        const val iconsCoreArtifact = "material-icons-core"
        const val iconsExtendedArtifact = "material-icons-extended"
        const val componentsGroup = "org.jetbrains.compose.components"
        const val resourcesArtifact = "components-resources"

        fun runtime() = "$runtimeGroup:$runtimeArtifact:$composeMultiplatform"

        fun foundation() = "$foundationGroup:$foundationArtifact:$composeMultiplatform"

        fun ui() = "$uiGroup:$uiArtifact:$composeMultiplatform"

        fun material3() = "$material3Group:$material3Artifact:$material3Version"

        fun iconsCore() = "$iconsCoreGroup:$iconsCoreArtifact:$iconsCoreVersion"

        fun iconsExtended() = "$iconsCoreGroup:$iconsExtendedArtifact:$iconsCoreVersion"

        fun resources() = "$componentsGroup:$resourcesArtifact:$composeMultiplatform"
    }

    // ── Jetpack Compose (Android) ───────────────────────────────────────────

    object JetpackCompose {
        const val group = "androidx.compose"

        fun foundation() = "$group.foundation:foundation:$jetpackComposeVersion"

        fun material3() = "androidx.compose.material3:material3:1.3.1"

        fun ui() = "$group.ui:ui:$jetpackComposeVersion"

        fun activity() = "androidx.activity:activity-compose:1.9.3"

        fun viewmodel() = "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"

        fun icons() = "$group.material:material-icons-core:$jetpackComposeVersion"
    }
}
