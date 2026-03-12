package org.hnau.plugins

import org.hnau.plugins.utils.versions.Aliased
import org.hnau.plugins.utils.versions.ComposeDependencyTypeValues
import org.hnau.plugins.utils.versions.GroupId
import org.hnau.plugins.utils.versions.AnnotationWithProcessor
import org.hnau.plugins.utils.versions.LibraryId
import org.hnau.plugins.utils.versions.PluginId
import org.hnau.plugins.utils.versions.Version
import org.hnau.plugins.utils.versions.Versioned
import org.hnau.plugins.utils.versions.withAlias
import org.hnau.plugins.utils.versions.withArtifact
import org.hnau.plugins.utils.versions.withVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal object Versions {

    // Android SDK
    val compileSdk = 36
    val minSdk = 23

    // JVM bytecode — single declaration, used for toolchain and compilerOptions
    val jvmTargetInt = 17
    val jvmTarget = JvmTarget.fromTarget(jvmTargetInt.toString())


    object Plugins {
        val kotlinMultiplatform =
            PluginId("org.jetbrains.kotlin.multiplatform") withVersion Version.Kotlin withAlias "kotlin-multiplatform"

        val kotlinJvm =
            PluginId("org.jetbrains.kotlin.jvm") withVersion Version.Kotlin withAlias "kotlin-jvm"

        val kotlinAndroid =
            PluginId("org.jetbrains.kotlin.android") withVersion Version.Kotlin withAlias "kotlin-android"

        val kotlinSerialization =
            PluginId("org.jetbrains.kotlin.plugin.serialization") withVersion Version.Kotlin withAlias "kotlin-serialization"

        val kotlinCompose =
            PluginId("org.jetbrains.kotlin.plugin.compose") withVersion Version.Kotlin withAlias "kotlin-compose"

        val androidMultiplatformLibrary =
            PluginId("com.android.kotlin.multiplatform.library") withVersion Version.AndroidGradlePlugin withAlias "android-multiplatformLibrary"

        val androidApplication =
            PluginId("com.android.application") withVersion Version.AndroidGradlePlugin withAlias "android-application"


        val googleServices =
            PluginId("com.google.gms.google-services") withVersion Version.GoogleServicesPlugin withAlias "googleServices"

        val ksp =
            PluginId("com.google.devtools.ksp") withVersion Version.KspPlugin withAlias "ksp"

        val composeMultiplatform =
            PluginId("org.jetbrains.compose") withVersion Version.ComposeMultiplatform withAlias "composeMultiplatform"

        val dokka =
            PluginId("org.jetbrains.dokka") withVersion Version.DokkaPlugin withAlias "dokka"

        val vanniktech =
            PluginId("com.vanniktech.maven.publish") withVersion Version.VanniktechPlugin withAlias "vanniktech"

        val signing: PluginId =
            PluginId("signing")

        //update in build.gradle.kts
        val hnauProject: List<Aliased<Versioned<PluginId>>> = listOf("jvm", "kmp", "ui", "androidapp").map { suffix ->
            PluginId("org.hnau.plugin.$suffix") withVersion Version.HnauPlugins withAlias "hnau-$suffix"
        }
    }

    object HnauCommons {
        val group = GroupId("org.hnau.commons")

        val kotlin = group withArtifact "kotlin" withVersion Version.HnauCommons

        val appModel = group withArtifact "app-model" withVersion Version.HnauCommons withAlias "commons-app-model"

        val appProjector = group withArtifact "app-projector" withVersion Version.HnauCommons withAlias "commons-app-projector"

        val gen: List<AnnotationWithProcessor<Versioned<LibraryId>>> = listOf(
            "pipe", "loggable", "sealup", "enumvalues",
        ).map { type ->
            AnnotationWithProcessor(
                annotation = "annotations",
                processor = "processor",
            ).map { suffix ->
                group withArtifact "gen-$type-$suffix" withVersion Version.HnauCommons
            }
        }
    }

    object Kotlinx {
        private val group = GroupId("org.jetbrains.kotlinx")

        val serialization = listOf("core", "json", "cbor").map { suffix ->
            group withArtifact "kotlinx-serialization-$suffix" withVersion Version.KotlinxSerialization
        }

        val immutable: Aliased<Versioned<LibraryId>> =
            group withArtifact "kotlinx-collections-immutable" withVersion Version.KotlinImmutable withAlias "kotlin-immutable"
    }

    val composeMultiplatform: ComposeDependencyTypeValues<Versioned<LibraryId>> = ComposeDependencyTypeValues(
        runtime = "org.jetbrains.compose.runtime" withArtifact "runtime" withVersion Version.ComposeMultiplatform,
        foundation = "org.jetbrains.compose.foundation" withArtifact "foundation" withVersion Version.ComposeMultiplatform,
        ui = "org.jetbrains.compose.ui" withArtifact "ui" withVersion Version.ComposeMultiplatform,
        material3 = "org.jetbrains.compose.material3" withArtifact "material3" withVersion Version.ComposeMultiplatformMaterial3,
        iconsCore = "org.jetbrains.compose.material" withArtifact "material-icons-core" withVersion Version.CommposeMultiplatformIcons,
        iconsExtended = "org.jetbrains.compose.material" withArtifact "material-icons-extended" withVersion Version.CommposeMultiplatformIcons,
    )

    object Jetpack {

        val compose: ComposeDependencyTypeValues<Versioned<LibraryId>> = ComposeDependencyTypeValues(
            runtime = "androidx.compose.runtime" withArtifact "runtime" withVersion Version.JetpackCompose,
            foundation = "androidx.compose.foundation" withArtifact "foundation" withVersion Version.JetpackCompose,
            material3 = "androidx.compose.material3" withArtifact "material3" withVersion Version.JetpackComposeMaterial3,
            ui = "androidx.compose.ui" withArtifact "ui" withVersion Version.JetpackCompose,
            iconsCore = "androidx.compose.material" withArtifact "material-icons-core" withVersion Version.JetpackCompose,
            iconsExtended = "androidx.compose.material" withArtifact "material-icons-extended" withVersion Version.JetpackCompose,
        )


        val activity: Versioned<LibraryId> =
            "androidx.activity" withArtifact "activity-compose" withVersion Version.ActivityCompose

        val viewmodel: Versioned<LibraryId> =
            "androidx.lifecycle" withArtifact "lifecycle-viewmodel-compose" withVersion Version.LifecycleViewmodelCompose
    }
}
