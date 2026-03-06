package org.hnau.plugins.project

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.HnauCommons
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class HnauProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create<HnauProjectExtension>("hnau")

        project.afterEvaluate {
            val ext = project.extensions.getByType(HnauProjectExtension::class.java)
            configure(project, ext)
        }
    }

    private fun configure(
        project: Project,
        ext: HnauProjectExtension,
    ) {
        val artifactId = project.path.removePrefix(":").replace(":", "-")
        val group = project.group.toString()
        val namespace =
            when {
                group.isNotEmpty() -> "$group.$artifactId"
                else -> artifactId
            }

        when (val moduleType = ext.module) {
            is ModuleType.Jvm -> configureJvm(project)
            is ModuleType.Kmp -> configureKmp(project, moduleType, namespace)
        }

        if (ext.ksp.isActive) {
            applyKsp(project, ext.ksp, ext.module)
        }

        if (ext.serialization) {
            applyKotlinSerialization(project)
        }

        val includeHnauCommons =
            ext.includeHnauCommons
                ?: project.extensions.extraProperties
                    .properties["hnau.includeHnauCommons"] as? Boolean
                ?: true

        if (includeHnauCommons) {
            addDep(project, HnauCommons.dep(HnauCommons.kotlin))
        }

        if (ext.module is ModuleType.Kmp) {
            val kmp = ext.module as ModuleType.Kmp
            if (kmp.app) {
                addDep(project, HnauCommons.dep(HnauCommons.appModel))
                if (kmp.compose) {
                    addDep(project, HnauCommons.dep(HnauCommons.appProjector))
                }
            }
        }

        configurePublishing(project, artifactId, ext)

        project.tasks
            .withType(KotlinCompilationTask::class.java)
            .configureEach { task ->
                task.compilerOptions {
                    freeCompilerArgs.add("-Xjsr305=strict")
                    if (this is KotlinJvmCompilerOptions) {
                        jvmTarget.set(Versions.jvmTarget)
                    }
                }
            }
    }

    private fun addDep(
        project: Project,
        notation: String,
    ) {
        val configuration =
            if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                "commonMainImplementation"
            } else {
                "implementation"
            }
        project.dependencies.add(configuration, notation)
    }

    private fun configureJvm(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")
        project.plugins.apply("java-library")

        project.extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(Versions.jvmTargetInt)
            compilerOptions {
                jvmTarget.set(Versions.jvmTarget)
            }
        }
    }

    private fun configureKmp(
        project: Project,
        kmp: ModuleType.Kmp,
        namespace: String,
    ) {
        project.plugins.apply("org.jetbrains.kotlin.multiplatform")
        project.plugins.apply("com.android.kotlin.multiplatform.library")

        if (kmp.compose) {
            project.plugins.apply("org.jetbrains.compose")
            project.plugins.apply("org.jetbrains.kotlin.plugin.compose")
        }

        val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        (kotlinExtension as ExtensionAware)
            .extensions
            .configure<KotlinMultiplatformAndroidLibraryExtension> {
                this.namespace = namespace
                compileSdk = Versions.compileSdk
                minSdk = Versions.minSdk
            }

        kotlinExtension.apply {
            if (kmp.compose) {
                jvm("desktop") {
                    withSourcesJar()
                }
            } else {
                jvm {
                    withSourcesJar()
                }
                linuxX64()
            }
        }
    }

    private fun applyKsp(
        project: Project,
        ksp: KspExtension,
        moduleType: ModuleType,
    ) {
        project.plugins.apply("com.google.devtools.ksp")

        fun kspAllTargets(notation: String) =
            when (moduleType) {
                is ModuleType.Jvm -> project.dependencies.add("ksp", notation)
                is ModuleType.Kmp -> {
                    project.dependencies.add("kspCommonMainMetadata", notation)
                    project.dependencies.add("kspAndroid", notation)
                    if (moduleType.compose) {
                        project.dependencies.add("kspDesktop", notation)
                    } else {
                        project.dependencies.add("kspJvm", notation)
                        project.dependencies.add("kspLinuxX64", notation)
                    }
                }
            }

        fun kspProcessor(
            annotations: String,
            processor: String,
        ) = when (moduleType) {
            is ModuleType.Jvm -> {
                project.dependencies.add("implementation", annotations)
                project.dependencies.add("ksp", processor)
            }
            is ModuleType.Kmp -> {
                project.dependencies.add("commonMainImplementation", annotations)
                kspAllTargets(processor)
            }
        }

        if (ksp.pipe) {
            kspProcessor(
                annotations = HnauCommons.dep(HnauCommons.Gen.pipeAnnotations),
                processor = HnauCommons.dep(HnauCommons.Gen.pipeProcessor),
            )
        }
        if (ksp.sealUp) {
            kspProcessor(
                annotations = HnauCommons.dep(HnauCommons.Gen.sealUpAnnotations),
                processor = HnauCommons.dep(HnauCommons.Gen.sealUpProcessor),
            )
        }
        if (ksp.enumValues) {
            kspProcessor(
                annotations = HnauCommons.dep(HnauCommons.Gen.enumValuesAnnotations),
                processor = HnauCommons.dep(HnauCommons.Gen.enumValuesProcessor),
            )
        }
        if (ksp.loggable) {
            kspProcessor(
                annotations = HnauCommons.dep(HnauCommons.Gen.loggableAnnotations),
                processor = HnauCommons.dep(HnauCommons.Gen.loggableProcessor),
            )
        }
        if (ksp.arrowOptics) {
            kspAllTargets(Versions.Arrow.opticsKspPlugin())
        }

        if (moduleType is ModuleType.Kmp) {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlinExtension.sourceSets.getByName("commonMain").kotlin.srcDir(
                project.layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"),
            )

            project.tasks
                .withType(KotlinCompilationTask::class.java)
                .configureEach { task ->
                    if (task.name != "kspCommonMainKotlinMetadata") {
                        task.dependsOn("kspCommonMainKotlinMetadata")
                    }
                }
        }
    }

    private fun applyKotlinSerialization(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.plugin.serialization")
        addDep(project, Versions.Kotlinx.serializationCore())
        addDep(project, Versions.Kotlinx.serializationJson())
    }

    private fun configurePublishing(
        project: Project,
        artifactId: String,
        ext: HnauProjectExtension,
    ) {
        val publishExt = ext.publish
        val gitUrl =
            publishExt.gitUrl
                ?: throw GradleException(
                    "publish.gitUrl is required in module '${project.path}'. " +
                        "Set it via: publish { gitUrl = \"https://github.com/user/repo\" }",
                )
        val description = publishExt.description ?: project.name
        val licenseId = publishExt.license
        val licenseUrl = spdxLicenseUrl(licenseId)
        val isKmp = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
        val dokkaTaskName = "dokkaGeneratePublicationHtml"

        project.plugins.apply("org.jetbrains.dokka")
        project.plugins.apply("com.vanniktech.maven.publish")
        project.plugins.apply("signing")

        project.extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()

            val platform =
                when (isKmp) {
                    true ->
                        KotlinMultiplatform(
                            javadocJar = JavadocJar.Dokka(dokkaTaskName),
                            sourcesJar = SourcesJar.Sources(),
                        )
                    false ->
                        KotlinJvm(
                            javadocJar = JavadocJar.Dokka(dokkaTaskName),
                            sourcesJar = SourcesJar.Sources(),
                        )
                }
            configure(platform)

            coordinates(
                groupId = project.group.toString(),
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

        project.extensions.configure<SigningExtension> {
            val keyId = project.providers.gradleProperty("signing.keyId").orNull
            val password = project.providers.gradleProperty("signing.password").orNull
            val secretKey = project.providers.gradleProperty("signing.secretKey").orNull
            if (secretKey != null && password != null) {
                useInMemoryPgpKeys(keyId, secretKey, password)
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
