package org.hnau.plugins.project

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.vanniktech.maven.publish.*
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.hnau.plugins.ConfigurationNames
import org.hnau.plugins.TaskNames
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.PluginIds
import org.hnau.plugins.project.extensions.KmpExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ModuleConfigurator(
    private val project: Project,
    private val publishingConfigurator: PublishingConfigurator,
) {
    private var moduleType: ModuleType? = null

    fun getModuleType(): ModuleType? = moduleType

    fun jvm(includeHnauCommons: Boolean? = null) {
        check(moduleType == null) { "jvm() or kmp() already called for module '${project.path}'" }
        moduleType = ModuleType.Jvm

        if (!project.plugins.hasPlugin(PluginIds.kotlinJvm)) {
            project.plugins.apply(PluginIds.kotlinJvm)
        }
        val jvmVersion = JavaVersion.toVersion(Versions.jvmTargetInt)
        project.extensions.configure<JavaPluginExtension> {
            sourceCompatibility = jvmVersion
            targetCompatibility = jvmVersion
        }
        project.extensions.configure<MavenPublishBaseExtension> {
            configure(
                KotlinJvm(
                    javadocJar = JavadocJar.Dokka(TaskNames.dokkaGeneratePublicationHtml),
                    sourcesJar = SourcesJar.Sources(),
                ),
            )
        }
        publishingConfigurator.configure()
        if (resolveIncludeHnauCommons(includeHnauCommons)) {
            addDep(HnauCommons.dep(HnauCommons.kotlin))
        }
    }

    fun kmp(
        includeHnauCommons: Boolean? = null,
        kmpExtension: KmpExtension,
    ) {
        check(moduleType == null) { "jvm() or kmp() already called for module '${project.path}'" }
        moduleType = ModuleType.Kmp(compose = kmpExtension.compose, app = kmpExtension.app)

        if (kmpExtension.compose) {
            if (!project.plugins.hasPlugin(PluginIds.composeMultiplatform)) {
                project.plugins.apply(PluginIds.composeMultiplatform)
            }
            if (!project.plugins.hasPlugin(PluginIds.kotlinCompose)) {
                project.plugins.apply(PluginIds.kotlinCompose)
            }
        }
        if (!project.plugins.hasPlugin(PluginIds.kotlinMultiplatform)) {
            project.plugins.apply(PluginIds.kotlinMultiplatform)
        }
        if (!project.plugins.hasPlugin(PluginIds.androidKmpLibrary)) {
            project.plugins.apply(PluginIds.androidKmpLibrary)
        }

        val artifactId = project.path.removePrefix(":").replace(":", "-")
        val group =
            project.extensions.extraProperties.properties["hnau.group"] as? String
                ?: throw GradleException(
                    "allModules.group is required. Set it via allModules { group = \"com.example\" } in settings.",
                )
        val namespace = "$group.${artifactId.replace('-', '.')}"

        val kotlinExtension = project.extensions.getByType<KotlinMultiplatformExtension>()

        (kotlinExtension as ExtensionAware)
            .extensions
            .configure<KotlinMultiplatformAndroidLibraryExtension> {
                this.namespace = namespace
                compileSdk = Versions.compileSdk
                minSdk = Versions.minSdk
            }

        kotlinExtension.apply {
            if (kmpExtension.compose) {
                jvm("desktop") { withSourcesJar() }
            } else {
                jvm { withSourcesJar() }
                linuxX64()
            }
        }

        project.extensions.configure<MavenPublishBaseExtension> {
            configure(
                KotlinMultiplatform(
                    javadocJar = JavadocJar.Dokka(TaskNames.dokkaGeneratePublicationHtml),
                    sourcesJar = SourcesJar.Sources(),
                ),
            )
        }

        publishingConfigurator.configure()

        if (kmpExtension.compose) {
            addDep(Versions.Compose.runtime())
            addDep(Versions.Compose.foundation())
            addDep(Versions.Compose.ui())
            addDep(Versions.Compose.material3())
            addDep(Versions.Compose.iconsCore())
        }

        if (resolveIncludeHnauCommons(includeHnauCommons)) {
            addDep(HnauCommons.dep(HnauCommons.kotlin))
            if (kmpExtension.app) {
                addDep(HnauCommons.dep(HnauCommons.appModel))
                if (kmpExtension.compose) {
                    addDep(HnauCommons.dep(HnauCommons.appProjector))
                }
            }
        }
    }

    private fun addDep(notation: String) {
        val configuration =
            if (project.plugins.hasPlugin(PluginIds.kotlinMultiplatform)) {
                ConfigurationNames.commonMainImplementation
            } else {
                ConfigurationNames.implementation
            }
        project.dependencies.add(configuration, notation)
    }

    private fun resolveIncludeHnauCommons(localOverride: Boolean?): Boolean =
        localOverride
            ?: project.extensions.extraProperties.properties["hnau.includeHnauCommons"] as? Boolean
            ?: true
}
