package org.hnau.plugins.project

import com.android.build.api.dsl.ApplicationExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.JavaCompile
import org.hnau.plugins.ConfigurationNames
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.Kotlinx
import org.hnau.plugins.Versions.PluginIds
import org.hnau.plugins.settings.HnauSettingsContainer
import org.hnau.plugins.settings.PublishSettings
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
        val settings = getSettings(project)

        // Configure based on module type
        when (type) {
            ModuleType.JVM -> configureJvm(project)
            ModuleType.KMP -> configureKmp(project, settings)
            ModuleType.UI -> configureUi(project, settings)
            ModuleType.ANDROID_APP -> configureAndroidApp(project, settings)
        }

        // Common configuration for all types
        // Note: We check for serialization/ksp AFTER configuring the base module
        // because user should declare them BEFORE org.hnau plugins
        configureCommon(project, settings, type)
    }

    private fun getSettings(project: Project): HnauSettingsContainer =
        project.rootProject.extensions.findByType(HnauSettingsContainer::class.java)
            ?: HnauSettingsContainer().apply {
                includeCommonsKotlinDependency = true
            }

    private fun configureJvm(project: Project) {
        project.plugins.apply(PluginIds.kotlinJvm)

        val kotlinExtension = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
        kotlinExtension.jvmToolchain(Versions.jvmTargetInt)

        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions {
                jvmTarget.set(Versions.jvmTarget)
            }
        }

        project.tasks.withType(JavaCompile::class.java).configureEach { task ->
            task.options.release.set(Versions.jvmTargetInt)
        }
    }

    private fun configureKmp(
        project: Project,
        settings: HnauSettingsContainer,
    ) {
        project.plugins.apply(PluginIds.kotlinMultiplatform)
        project.plugins.apply(PluginIds.androidKmpLibrary)

        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        val androidExtension =
            (kotlinExtension as ExtensionAware)
                .extensions
                .getByType(com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension::class.java)

        val artifactId = calculateArtifactId(project)
        val namespace = "${settings.publishSettings?.groupId}.${artifactId.replace('-', '.')}"

        androidExtension.apply {
            this.namespace = namespace
            compileSdk = Versions.compileSdk
            minSdk = Versions.minSdk
        }

        kotlinExtension.jvm { withSourcesJar() }
        kotlinExtension.linuxX64()
    }

    private fun configureUi(
        project: Project,
        settings: HnauSettingsContainer,
    ) {
        project.plugins.apply(PluginIds.kotlinMultiplatform)
        project.plugins.apply(PluginIds.androidKmpLibrary)
        project.plugins.apply(PluginIds.composeMultiplatform)
        project.plugins.apply(PluginIds.kotlinCompose)

        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        val androidExtension =
            (kotlinExtension as ExtensionAware)
                .extensions
                .getByType(com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension::class.java)

        val artifactId = calculateArtifactId(project)
        val namespace = "${settings.publishSettings?.groupId}.${artifactId.replace('-', '.')}"

        androidExtension.apply {
            this.namespace = namespace
            compileSdk = Versions.compileSdk
            minSdk = Versions.minSdk
        }

        // Create desktop target - Compose Desktop plugin will reconfigure it later
        kotlinExtension.jvm("desktop") { withSourcesJar() }

        // Add compose desktop dependency for desktopMain source set (auto-detects OS/arch)
        val composeDeps =
            org.jetbrains.compose.ComposePlugin
                .Dependencies(project)
        project.dependencies.add("desktopMainImplementation", composeDeps.desktop.currentOs)

        val artifactIdClean = artifactId.removePrefix("pinfin-")
        val resourcesPackage = "hnau.pinfin.${artifactIdClean.replace('-', '.')}.resources"

        (project.extensions.findByName("compose") as? org.jetbrains.compose.ComposeExtension)?.let { composeExt ->
            (composeExt as ExtensionAware)
                .extensions
                .configure(org.jetbrains.compose.resources.ResourcesExtension::class.java) {
                    it.packageOfResClass = resourcesPackage
                }
        }

        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.runtime())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.foundation())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.ui())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.material3())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.iconsCore())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.iconsExtended())
        project.dependencies.add(ConfigurationNames.commonMainImplementation, Versions.Compose.resources())
    }

    private fun configureAndroidApp(
        project: Project,
        settings: HnauSettingsContainer,
    ) {
        project.plugins.apply(PluginIds.androidApplication)
        project.plugins.apply(PluginIds.kotlinAndroid)
        project.plugins.apply(PluginIds.kotlinCompose)

        val androidExtension = project.extensions.getByType(ApplicationExtension::class.java)
        androidExtension.apply {
            compileSdk = Versions.compileSdk
            defaultConfig {
                minSdk = Versions.minSdk
            }
            compileOptions {
                sourceCompatibility =
                    org.gradle.api.JavaVersion
                        .toVersion(Versions.jvmTargetInt)
                targetCompatibility =
                    org.gradle.api.JavaVersion
                        .toVersion(Versions.jvmTargetInt)
            }
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            task.compilerOptions {
                jvmTarget.set(Versions.jvmTarget)
            }
        }

        project.dependencies.add("implementation", Versions.JetpackCompose.foundation())
        project.dependencies.add("implementation", Versions.JetpackCompose.material3())
        project.dependencies.add("implementation", Versions.JetpackCompose.ui())
        project.dependencies.add("implementation", Versions.JetpackCompose.activity())
        project.dependencies.add("implementation", Versions.JetpackCompose.viewmodel())
        project.dependencies.add("implementation", Versions.JetpackCompose.icons())
    }

    private fun configureCommon(
        project: Project,
        settings: HnauSettingsContainer,
        type: ModuleType,
    ) {
        project.plugins.apply(PluginIds.vanniktech)

        if (settings.includeCommonsKotlinDependency) {
            val configName =
                if (type == ModuleType.JVM || type == ModuleType.ANDROID_APP) {
                    "implementation"
                } else {
                    ConfigurationNames.commonMainImplementation
                }
            project.dependencies.add(configName, HnauCommons.dep(HnauCommons.kotlin))
        }

        // Check for plugins that may be applied AFTER org.hnau plugins using withId
        project.plugins.withId(PluginIds.kotlinSerialization) {
            configureSerialization(project, type)
        }

        project.plugins.withId(PluginIds.ksp) {
            configureKsp(project, type)
        }

        settings.publishSettings?.let { publishSettings ->
            configurePublishing(project, publishSettings, type)
        }
    }

    private fun configureSerialization(
        project: Project,
        type: ModuleType,
    ) {
        val configName =
            if (type == ModuleType.JVM || type == ModuleType.ANDROID_APP) {
                "implementation"
            } else {
                ConfigurationNames.commonMainImplementation
            }

        project.dependencies.add(configName, Kotlinx.serializationCore())
        project.dependencies.add(configName, Kotlinx.serializationJson())
    }

    private fun configureKsp(
        project: Project,
        type: ModuleType,
    ) {
        val processors =
            listOf(
                HnauCommons.dep(HnauCommons.Gen.pipeProcessor),
                HnauCommons.dep(HnauCommons.Gen.sealUpProcessor),
                HnauCommons.dep(HnauCommons.Gen.enumValuesProcessor),
                HnauCommons.dep(HnauCommons.Gen.loggableProcessor),
            )

        when (type) {
            ModuleType.JVM, ModuleType.ANDROID_APP -> {
                processors.forEach { processor ->
                    project.dependencies.add("ksp", processor)
                }
            }
            ModuleType.KMP, ModuleType.UI -> {
                processors.forEach { processor ->
                    project.dependencies.add("kspCommonMainMetadata", processor)
                }

                val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
                kotlinExtension.sourceSets.named("commonMain") { sourceSet: KotlinSourceSet ->
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
        settings: PublishSettings,
        type: ModuleType,
    ) {
        val artifactId = calculateArtifactId(project)

        project.extensions.configure(MavenPublishBaseExtension::class.java) { publishing ->
            publishing.publishToMavenCentral()

            publishing.pom { pom ->
                pom.name.set(settings.artifactId ?: artifactId)
                pom.description.set(settings.description ?: "${settings.groupId}:$artifactId")
                pom.url.set(settings.gitUrl)

                pom.licenses { licenses ->
                    licenses.license { license ->
                        license.name.set(settings.licenseName ?: "The Apache License, Version 2.0")
                        license.url.set(settings.licenseUrl ?: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                pom.developers { developers ->
                    developers.developer { developer ->
                        developer.name.set(settings.developerName ?: "HNAU")
                        developer.email.set(settings.developerEmail)
                    }
                }

                pom.scm { scm ->
                    scm.connection.set("scm:git:${settings.gitUrl}")
                    scm.developerConnection.set("scm:git:${settings.gitUrl}")
                    scm.url.set(settings.gitUrl)
                }
            }
        }

        project.group = settings.groupId
        settings.version?.let { project.version = it }
    }

    private fun calculateArtifactId(project: Project): String = project.path.removePrefix(":").replace(":", "-")
}
