package org.hnau.plugins.project

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.JavaCompile
import org.hnau.plugins.Versions
import org.hnau.plugins.utils.SharedConfig
import org.hnau.plugins.utils.versions.ComposeDependencyType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Core configurator for all module types.
 * Called from entry point plugins with their specific ModuleType.
 */
object ModuleConfigurator {

    fun configure(
        project: Project,
        type: ModuleType,
    ) {
        val config = SharedConfig
            .extractFromRootProject(project)
            .toProjectConfig(project)

        val projectType: ProjectType = when (type) {
            ModuleType.JVM -> configureJvm(project, config, false)
            ModuleType.KMP -> configureKmp(project, config, false)
            ModuleType.UI -> configureKmp(project, config, true)
            ModuleType.ANDROID_APP -> configureJvm(project, config, true)
        }

        configureCommon(
            project = project,
            config = config,
            projectType = projectType
        )
    }

    private fun configureJvm(
        project: Project,
        config: ProjectConfig,
        addAndroid: Boolean,
    ): ProjectType {

        val projectType = ProjectType.Jvm

        when (addAndroid) {
            false -> {
                project.applyPlugin(Versions.Plugins.kotlinJvm.withoutAlias.withoutVersion)

                project
                    .extensions
                    .getByType(KotlinJvmProjectExtension::class.java).jvmToolchain(Versions.jvmTargetInt)

                project.tasks.withType(JavaCompile::class.java).configureEach { task ->
                    task.options.release.set(Versions.jvmTargetInt)
                }
            }

            true -> {
                project.applyPlugin(Versions.Plugins.googleServices.withoutAlias.withoutVersion)
                project.applyPlugin(Versions.Plugins.androidApplication.withoutAlias.withoutVersion)
                project.applyPlugin(Versions.Plugins.kotlinAndroid.withoutAlias.withoutVersion)
                project.applyPlugin(Versions.Plugins.kotlinCompose.withoutAlias.withoutVersion)

                project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                    task.compilerOptions {
                        jvmTarget.set(Versions.jvmTarget)
                    }
                }

                project
                    .extensions
                    .getByType(ApplicationExtension::class.java)
                    .apply {
                        namespace = config.androidNamespace
                        compileSdk = Versions.compileSdk
                        defaultConfig {
                            minSdk = Versions.minSdk
                        }
                        compileOptions {
                            val javaVersion = JavaVersion.toVersion(Versions.jvmTargetInt)
                            sourceCompatibility = javaVersion
                            targetCompatibility = javaVersion
                        }
                    }

                buildList {
                    addAll(
                        ComposeDependencyType
                            .entries
                            .map(Versions.Jetpack.compose::get)
                    )
                    add(Versions.Jetpack.activity)
                    add(Versions.Jetpack.viewmodel)
                }.forEach { composeMultiplatformDependency ->
                    project.addDependency(
                        type = projectType,
                        dependency = composeMultiplatformDependency,
                    )
                }

            }
        }

        return projectType
    }

    private fun configureKmp(
        project: Project,
        config: ProjectConfig,
        addCompose: Boolean,
    ): ProjectType.Kmp {
        project.applyPlugin(Versions.Plugins.kotlinMultiplatform.withoutAlias.withoutVersion)
        project.applyPlugin(Versions.Plugins.androidMultiplatformLibrary.withoutAlias.withoutVersion)

        if (addCompose) {
            project.applyPlugin(Versions.Plugins.composeMultiplatform.withoutAlias.withoutVersion)
            project.applyPlugin(Versions.Plugins.kotlinCompose.withoutAlias.withoutVersion)
        }

        val projectType = ProjectType.Kmp(
            kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        )

        val androidExtension = (projectType.kmpExtension as ExtensionAware)
            .extensions
            .getByType(KotlinMultiplatformAndroidLibraryExtension::class.java)

        androidExtension.apply {
            namespace = config.androidNamespace
            compileSdk = Versions.compileSdk
            minSdk = Versions.minSdk
        }

        when (addCompose) {
            true -> {
                projectType
                    .kmpExtension
                    .jvm(DesktopTargetName) {
                        withSourcesJar()
                    }

                project.dependencies.add(
                    "${DesktopTargetName}MainImplementation",
                    ComposePlugin.Dependencies(project).desktop.currentOs,
                )

                (project.extensions.findByName("compose") as ExtensionAware)
                    .extensions
                    .configure(ResourcesExtension::class.java) { resources ->
                        resources.packageOfResClass = "${config.androidNamespace}.resources"
                    }

                ComposeDependencyType
                    .entries
                    .map(Versions.composeMultiplatform::get)
                    .forEach { composeMultiplatformDependency ->
                        project
                            .addDependency(
                                type = projectType,
                                dependency = composeMultiplatformDependency,
                            )
                    }
            }

            false -> {

                projectType
                    .kmpExtension
                    .jvm {
                        withSourcesJar()
                    }

                projectType
                    .kmpExtension
                    .linuxX64()
            }
        }

        return projectType
    }

    private fun configureCommon(
        project: Project,
        config: ProjectConfig,
        projectType: ProjectType,
    ) {

        if (config.asLibraryId != Versions.HnauCommons.kotlin.withoutVersion) {
            project.addDependency(
                type = projectType,
                dependency = Versions.HnauCommons.kotlin,
            )
        }

        configureSerializationIfNeed(
            project = project,
            projectType = projectType,
        )

        configureKspIdNeed(
            project = project,
            projectType = projectType,
        )

        config.publish?.let { publish ->
            configurePublishing(
                project = project,
                publish = publish,
                projectConfig = config,
                projectType = projectType,
            )
        }
    }

    private fun configureSerializationIfNeed(
        project: Project,
        projectType: ProjectType,
    ) {
        if (!project.hasPlugin(Versions.Plugins.kotlinSerialization.withoutAlias.withoutVersion)) {
            return
        }

        Versions
            .Kotlinx
            .serialization
            .forEach { dependency ->
                project.addDependency(
                    type = projectType,
                    dependency = dependency,
                )
            }
    }

    private fun configureKspIdNeed(
        project: Project,
        projectType: ProjectType,
    ) {
        if (!project.hasPlugin(Versions.Plugins.ksp.withoutAlias.withoutVersion)) {
            return
        }

        Versions.HnauCommons.gen.forEach { annotationWithProcessor ->
            project.addDependency(
                type = projectType,
                dependency = annotationWithProcessor.annotation,
            )
            project.addDependency(
                configurationName = when (projectType) {
                    ProjectType.Jvm -> "ksp"
                    is ProjectType.Kmp -> "kspCommonMainMetadata"
                },
                dependency = annotationWithProcessor.processor,
            )
        }

        when (projectType) {
            ProjectType.Jvm -> Unit
            is ProjectType.Kmp -> {
                projectType.commonMainSourceSet.configure { sourceSet: KotlinSourceSet ->
                    sourceSet.kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
                }

                project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                    if (task.name != "kspCommonMainKotlinMetadata") {
                        task.dependsOn("kspCommonMainKotlinMetadata")
                    }
                }
            }
        }
    }

    private fun configurePublishing(
        project: Project,
        publish: ProjectConfig.Publish,
        projectConfig: ProjectConfig,
        projectType: ProjectType,
    ) {
        project.applyPlugin(Versions.Plugins.dokka.withoutAlias.withoutVersion)
        project.applyPlugin(Versions.Plugins.vanniktech.withoutAlias.withoutVersion)
        project.applyPlugin(Versions.Plugins.signing)


        project.extensions.configure(MavenPublishBaseExtension::class.java) { publishing ->
            publishing.publishToMavenCentral()

            publishing.pom { pom ->
                pom.name.set(projectConfig.artifactId.artifactId)
                pom.description.set(publish.description)
                pom.url.set(publish.gitUrl)

                pom.licenses { licenses ->
                    licenses.license { license ->
                        license.name.set(publish.licenseName)
                        license.url.set(publish.licenseUrl)
                    }
                }

                pom.developers { developers ->
                    developers.developer { developer ->
                        developer.name.set(publish.developerName)
                        developer.email.set(publish.developerEmail)
                    }
                }

                pom.scm { scm ->
                    scm.connection.set("scm:git:${publish.gitUrl}")
                    scm.developerConnection.set("scm:git:${publish.gitUrl}")
                    scm.url.set(publish.gitUrl)
                }
            }
        }

        project.group = projectConfig.groupId.groupId
        project.version = publish.version
    }
}

private const val DesktopTargetName = "desktop"
