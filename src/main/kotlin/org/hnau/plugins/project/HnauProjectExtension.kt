package org.hnau.plugins.project

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.Action
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
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

open class HnauProjectExtension(
    private val project: Project,
) {
    internal var module: ModuleType? = null

    val publish: PublishExtension = PublishExtension()

    fun publish(action: Action<PublishExtension>) {
        action.execute(publish)
    }

    val ksp: KspExtension = KspExtension()

    var serialization: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (!project.plugins.hasPlugin(PluginIds.kotlinSerialization)) {
                    project.plugins.apply(PluginIds.kotlinSerialization)
                }
                addDep(Versions.Kotlinx.serializationCore())
                addDep(Versions.Kotlinx.serializationJson())
            }
        }

    private fun resolveIncludeHnauCommons(localOverride: Boolean?): Boolean =
        localOverride
            ?: project.extensions.extraProperties.properties["hnau.includeHnauCommons"] as? Boolean
            ?: true

    private fun addDep(notation: String) {
        val configuration =
            if (project.plugins.hasPlugin(PluginIds.kotlinMultiplatform)) {
                ConfigurationNames.commonMainImplementation
            } else {
                ConfigurationNames.implementation
            }
        project.dependencies.add(configuration, notation)
    }

    fun jvm(includeHnauCommons: Boolean? = null) {
        check(module == null) { "jvm() or kmp() already called for module '${project.path}'" }
        module = ModuleType.Jvm

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
        configurePublishing()
        if (resolveIncludeHnauCommons(includeHnauCommons)) {
            addDep(HnauCommons.dep(HnauCommons.kotlin))
        }
    }

    fun kmp(
        includeHnauCommons: Boolean? = null,
        action: Action<KmpExtension> = Action {},
    ) {
        check(module == null) { "jvm() or kmp() already called for module '${project.path}'" }
        val kmp = KmpExtension()
        action.execute(kmp)
        module = ModuleType.Kmp(compose = kmp.compose, app = kmp.app)

        if (kmp.compose) {
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
        val namespace = "$group.$artifactId"

        val kotlinExtension = project.extensions.getByType<KotlinMultiplatformExtension>()

        (kotlinExtension as ExtensionAware)
            .extensions
            .configure<KotlinMultiplatformAndroidLibraryExtension> {
                this.namespace = namespace
                compileSdk = Versions.compileSdk
                minSdk = Versions.minSdk
            }

        kotlinExtension.apply {
            if (kmp.compose) {
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

        configurePublishing()

        if (kmp.compose) {
            addDep(Versions.Compose.runtime())
            addDep(Versions.Compose.foundation())
            addDep(Versions.Compose.ui())
            addDep(Versions.Compose.material3())
            addDep(Versions.Compose.iconsCore())
        }

        if (resolveIncludeHnauCommons(includeHnauCommons)) {
            addDep(HnauCommons.dep(HnauCommons.kotlin))
            if (kmp.app) {
                addDep(HnauCommons.dep(HnauCommons.appModel))
                if (kmp.compose) {
                    addDep(HnauCommons.dep(HnauCommons.appProjector))
                }
            }
        }
    }

    fun ksp(action: Action<KspExtension>) {
        val currentModule =
            checkNotNull(module) {
                "ksp() must be called after jvm() or kmp() in module '${project.path}'"
            }
        action.execute(ksp)
        if (ksp.isActive) {
            if (!project.plugins.hasPlugin(PluginIds.ksp)) {
                project.plugins.apply(PluginIds.ksp)
            }
            applyKspDeps(currentModule)
        }
    }

    private fun configurePublishing() {
        val artifactId = project.path.removePrefix(":").replace(":", "-")
        val groupId =
            publish.group
                ?: project.extensions.extraProperties.properties["hnau.group"] as? String
                ?: throw GradleException(
                    "allModules.group is required. Set it via allModules { group = \"com.example\" } in settings.",
                )
        val gitUrl =
            publish.gitUrl
                ?: project.extensions.extraProperties.properties["hnau.gitUrl"] as? String
                ?: throw GradleException(
                    "gitUrl is required. Set it via allModules { gitUrl = \"...\" } in settings or publish { gitUrl = \"...\" } in the module.",
                )
        val description = publish.description ?: project.name
        val licenseId = publish.license
        val licenseUrl = spdxLicenseUrl(licenseId)

        project.extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()

            coordinates(
                groupId = groupId,
                artifactId = artifactId,
                version = project.version.toString(),
            )

            pom { pom ->
                pom.name.set(project.name)
                pom.description.set(description)
                pom.url.set(gitUrl)
                pom.licenses { spec ->
                    spec.license { lic ->
                        lic.name.set(licenseId)
                        lic.url.set(licenseUrl)
                    }
                }
                pom.developers { spec ->
                    spec.developer { dev ->
                        dev.id.set("hnau256")
                        dev.name.set("Mark Zorikhin")
                        dev.email.set("hnau256@gmail.com")
                    }
                }
                pom.scm { scm ->
                    scm.connection.set("scm:git:$gitUrl.git")
                    scm.url.set(gitUrl)
                }
            }
        }

        project.extensions.extraProperties["mavenCentralUsername"] =
            project.providers.gradleProperty("mavenCentralUsername").orNull
        project.extensions.extraProperties["mavenCentralPassword"] =
            project.providers.gradleProperty("mavenCentralPassword").orNull

        project.extensions.configure<org.gradle.plugins.signing.SigningExtension> {
            val keyId = project.providers.gradleProperty("signing.keyId").orNull
            val password = project.providers.gradleProperty("signing.password").orNull
            val secretKey = project.providers.gradleProperty("signing.secretKey").orNull
            if (secretKey != null && password != null) {
                useInMemoryPgpKeys(keyId, secretKey, password)
            }
        }
    }

    private fun applyKspDeps(currentModule: ModuleType) {
        val includeHnauCommons =
            project.extensions.extraProperties.properties["hnau.includeHnauCommons"] as? Boolean
                ?: true

        fun kspAllTargets(notation: String) =
            when (currentModule) {
                is ModuleType.Jvm -> project.dependencies.add(ConfigurationNames.ksp, notation)
                is ModuleType.Kmp -> {
                    project.dependencies.add(ConfigurationNames.kspCommonMainMetadata, notation)
                    project.dependencies.add(ConfigurationNames.kspAndroid, notation)
                    if (currentModule.compose) {
                        project.dependencies.add(ConfigurationNames.kspDesktop, notation)
                    } else {
                        project.dependencies.add(ConfigurationNames.kspJvm, notation)
                        project.dependencies.add(ConfigurationNames.kspLinuxX64, notation)
                    }
                }
            }

        fun kspProcessor(
            annotations: String,
            processor: String,
        ) = when (currentModule) {
            is ModuleType.Jvm -> {
                project.dependencies.add(ConfigurationNames.implementation, annotations)
                project.dependencies.add(ConfigurationNames.ksp, processor)
            }
            is ModuleType.Kmp -> {
                project.dependencies.add(ConfigurationNames.commonMainImplementation, annotations)
                kspAllTargets(processor)
            }
        }

        if (includeHnauCommons) {
            if (ksp.pipe) kspProcessor(HnauCommons.dep(HnauCommons.Gen.pipeAnnotations), HnauCommons.dep(HnauCommons.Gen.pipeProcessor))
            if (ksp.sealUp) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.sealUpAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.sealUpProcessor),
                )
            }
            if (ksp.enumValues) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.enumValuesAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.enumValuesProcessor),
                )
            }
            if (ksp.loggable) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.loggableAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.loggableProcessor),
                )
            }
        }
        if (ksp.arrowOptics) {
            kspAllTargets(Versions.Arrow.opticsKspPlugin())
        }

        if (currentModule is ModuleType.Kmp) {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlinExtension.sourceSets.getByName("commonMain").kotlin.srcDir(
                project.layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"),
            )

            project.tasks
                .withType(KotlinCompilationTask::class.java)
                .configureEach { task ->
                    if (task.name != TaskNames.kspCommonMainKotlinMetadata) {
                        task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
                    }
                }

            project.tasks
                .matching { task -> task.name.startsWith("ksp") && task.name != TaskNames.kspCommonMainKotlinMetadata }
                .configureEach { task ->
                    task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
                }
        }
    }

    private fun spdxLicenseUrl(spdxId: String) =
        when (spdxId) {
            "MIT" -> "https://opensource.org/licenses/MIT"
            "Apache-2.0" -> "https://www.apache.org/licenses/LICENSE-2.0"
            "GPL-3.0" -> "https://www.gnu.org/licenses/gpl-3.0.html"
            "LGPL-2.1" -> "https://www.gnu.org/licenses/lgpl-2.1.html"
            else -> "https://opensource.org/licenses/$spdxId"
        }
}
