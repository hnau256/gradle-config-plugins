package org.hnau.plugins.project

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.builder.model.Dependencies
import com.android.tools.r8.internal.pr
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.signing.SigningExtension
import org.hnau.plugins.TaskNames
import org.hnau.plugins.Versions
import org.hnau.plugins.utils.SharedConfig
import org.hnau.plugins.utils.versions.ComposeDependencyType
import org.hnau.plugins.utils.versions.ComposeDependencyTypeValues
import org.hnau.plugins.utils.versions.LibraryId
import org.hnau.plugins.utils.versions.Versioned
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun configureProject(
    project: Project,
    moduleType: ModuleType,
) {
    val config = SharedConfig
        .extractFromRootProject(project)
        .toProjectConfig(project)

    val projectType: ProjectType = when (moduleType) {
        ModuleType.JVM -> configureJvm(
            project = project,
            config = config,
            addAndroid = false,
        )

        ModuleType.KMP -> configureKmp(
            project = project,
            config = config,
            addCompose = false,
        )

        ModuleType.UI -> configureKmp(
            project = project,
            config = config,
            addCompose = true,
        )

        ModuleType.ANDROID_APP -> configureJvm(
            project = project,
            config = config,
            addAndroid = true,
        )
    }

    configureCommon(
        project = project,
        config = config,
        projectType = projectType,
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
                .getByType(KotlinJvmProjectExtension::class.java)
                .jvmToolchain(Versions.jvmTargetInt)

            project
                .extensions
                .getByType(JavaPluginExtension::class.java)
                .targetCompatibility = JavaVersion.toVersion(Versions.jvmTargetInt)

            project.tasks.withType(JavaCompile::class.java).configureEach { task ->
                task.options.release.set(Versions.jvmTargetInt)
            }
        }

        true -> {
            project.applyPlugin(Versions.Plugins.googleServices.withoutAlias.withoutVersion)
            project.applyPlugin(Versions.Plugins.androidApplication.withoutAlias.withoutVersion)
            project.applyPlugin(Versions.Plugins.kotlinAndroid.withoutAlias.withoutVersion)
            project.applyPlugin(Versions.Plugins.kotlinCompose.withoutAlias.withoutVersion)

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

            project.addAndroidDependencies(
                projectType = projectType,
                addCompose = true,
            )

            project.addComposeDependencies(
                dependencies = Versions.jetpackCompose,
                projectType = projectType,
            )
        }
    }

    project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
        task.compilerOptions {
            jvmTarget.set(Versions.jvmTarget)
        }
    }

    return projectType
}

private fun Project.addComposeDependencies(
    dependencies: ComposeDependencyTypeValues<Versioned<LibraryId>>,
    projectType: ProjectType,
) {
    ComposeDependencyType
        .entries
        .map(dependencies::get)
        .forEach { jetpackComposeDependency ->
            addDependency(
                type = projectType,
                dependency = jetpackComposeDependency,
            )
        }
}

private fun configureKmp(
    project: Project,
    config: ProjectConfig,
    addCompose: Boolean,
): ProjectType.Kmp {
    project.applyPlugin(Versions.Plugins.kotlinMultiplatform.withoutAlias.withoutVersion)
    project.applyPlugin(Versions.Plugins.androidMultiplatformLibrary.withoutAlias.withoutVersion)

    project
        .extensions
        .getByType(KotlinMultiplatformExtension::class.java)
        .jvmToolchain(Versions.jvmTargetInt)

    if (addCompose) {
        project.applyPlugin(Versions.Plugins.composeMultiplatform.withoutAlias.withoutVersion)
        project.applyPlugin(Versions.Plugins.kotlinCompose.withoutAlias.withoutVersion)
    }

    val projectType = ProjectType.Kmp(
        kmpExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
    )

    project.addAndroidDependencies(
        projectType = projectType,
        addCompose = addCompose,
    )

    (projectType.kmpExtension as ExtensionAware)
        .extensions
        .getByType(KotlinMultiplatformAndroidLibraryExtension::class.java)
        .apply {
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

            project.addComposeDependencies(
                dependencies = Versions.composeMultiplatform,
                projectType = projectType,
            )
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

private fun Project.addAndroidDependencies(
    projectType: ProjectType,
    addCompose: Boolean,
) {
    buildList {
        addAll(Versions.Android.unconditioned)
        if (addCompose) {
            addAll(Versions.Android.unconditionedCompose)
        }
    }.forEach { dependency ->
        project.addDependency(
            configurationName = when (projectType) {
                ProjectType.Jvm -> "implementation"
                is ProjectType.Kmp -> "androidMainImplementation"
            },
            dependency = dependency,
        )
    }
}

private fun configureCommon(
    project: Project,
    config: ProjectConfig,
    projectType: ProjectType,
) {

    if (config.groupId != Versions.HnauCommons.group) {
        project.addDependency(
            type = projectType,
            dependency = Versions.HnauCommons.kotlin,
        )
    }

    buildList {
        addAll(Versions.Arrow.unconditioned)
        addAll(Versions.Kotlinx.unconditioned)
        addAll(Versions.Standalone.unconditioned)
    }.forEach { arrowDependency ->
        project.addDependency(
            type = projectType,
            dependency = arrowDependency,
        )
    }

    configureSerializationIfNeed(
        project = project,
        projectType = projectType,
    )

    val hasKsp = project.hasPlugin(Versions.Plugins.ksp.withoutAlias.withoutVersion)
    if (hasKsp) {
        configureKsp(
            project = project,
            projectType = projectType,
        )
    }

    config.publish?.let { publish ->
        configurePublishing(
            project = project,
            publish = publish,
            projectConfig = config,
            projectType = projectType,
            hasKsp = hasKsp,
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

private fun configureKsp(
    project: Project,
    projectType: ProjectType,
) {
    fun addProcessor(
        dependency: Versioned<LibraryId>,
    ) {
        project.addDependency(
            configurationName = when (projectType) {
                ProjectType.Jvm -> "ksp"
                is ProjectType.Kmp -> "kspCommonMainMetadata"
            },
            dependency = dependency,
        )
    }

    Versions.HnauCommons.gen.forEach { annotationWithProcessor ->
        project.addDependency(
            type = projectType,
            dependency = annotationWithProcessor.annotation,
        )
        addProcessor(annotationWithProcessor.processor)
    }

    addProcessor(Versions.Arrow.opticsProcessor)

    when (projectType) {
        ProjectType.Jvm -> Unit
        is ProjectType.Kmp -> {
            projectType.commonMainSourceSet.configure { sourceSet: KotlinSourceSet ->
                sourceSet.kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            }

            project.tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
                if (task.name == TaskNames.kspCommonMainKotlinMetadata) {
                    return@configureEach
                }
                task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
            }
            project.tasks.withType(AbstractCopyTask::class.java).configureEach { task ->
                task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
            }
        }
    }
}

private fun configurePublishing(
    project: Project,
    projectType: ProjectType,
    publish: ProjectConfig.Publish,
    projectConfig: ProjectConfig,
    hasKsp: Boolean,
) {
    project.applyPlugin(Versions.Plugins.vanniktech.withoutAlias.withoutVersion)
    project.applyPlugin(Versions.Plugins.dokka.withoutAlias.withoutVersion)
    project.applyPlugin(Versions.Plugins.signing)

    val depentDokkaByKsp = when (projectType) {
        ProjectType.Jvm -> false
        is ProjectType.Kmp -> hasKsp
    }
    if (depentDokkaByKsp) {
        project.tasks.named(TaskNames.dokkaGeneratePublicationHtml).configure { task ->
            task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
        }
    }

    project.extensions.extraProperties["mavenCentralUsername"] =
        project.providers.gradleProperty("mavenCentralUsername").orNull

    project.extensions.extraProperties["mavenCentralPassword"] =
        project.providers.gradleProperty("mavenCentralPassword").orNull

    project.extensions.configure<SigningExtension> {
        val keyId = project.providers.gradleProperty("signing.keyId").orNull
        val password = project.providers.gradleProperty("signing.password").orNull
        val secretKey = project.providers.gradleProperty("signing.secretKey").orNull
        if (secretKey != null && password != null) {
            useInMemoryPgpKeys(
                /* defaultKeyId = */ keyId,
                /* defaultSecretKey = */ secretKey,
                /* defaultPassword = */ password,
            )
        }
    }

    project.extensions.configure(MavenPublishBaseExtension::class.java) { publishing ->
        publishing.publishToMavenCentral()

        val dokkaJavadocJar = JavadocJar.Dokka(TaskNames.dokkaGeneratePublicationHtml)
        publishing.configure(
            when (projectType) {
                ProjectType.Jvm -> KotlinJvm(dokkaJavadocJar)
                is ProjectType.Kmp -> KotlinMultiplatform(dokkaJavadocJar)
            }
        )

        publishing.coordinates(
            groupId = projectConfig.groupId.groupId,
            artifactId = projectConfig.artifactId.artifactId,
            version = publish.version,
        )

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
}

private const val DesktopTargetName = "desktop"
