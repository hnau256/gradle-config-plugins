package org.hnau.plugins.project.projectext

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.signing.SigningExtension
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.Constants
import org.hnau.plugins.project.utils.ProjectConfig
import org.hnau.plugins.project.utils.ProjectType

internal fun Project.configurePublishing(
    projectType: ProjectType,
    publish: ProjectConfig.Publish,
    projectConfig: ProjectConfig,
    hasKsp: Boolean,
) {
    applyPlugin(Versions.Plugins.vanniktech.withoutAlias.withoutVersion)
    applyPlugin(Versions.Plugins.dokka.withoutAlias.withoutVersion)
    applyPlugin(Versions.Plugins.signing)

    val depentDokkaByKsp = when (projectType) {
        ProjectType.Jvm -> false
        is ProjectType.Kmp -> hasKsp
    }
    if (depentDokkaByKsp) {
        tasks.named(Constants.dokkaGeneratePublicationHtmlTaskName).configure { task ->
            task.dependsOn(Constants.kspCommonMainKotlinMetadataTaskName)
        }
    }

    extensions.extraProperties["mavenCentralUsername"] =
        providers.gradleProperty("mavenCentralUsername").orNull

    extensions.extraProperties["mavenCentralPassword"] =
        providers.gradleProperty("mavenCentralPassword").orNull

    extensions.configure<SigningExtension> {
        val keyId = providers.gradleProperty("signing.keyId").orNull
        val password = providers.gradleProperty("signing.password").orNull
        val secretKey = providers.gradleProperty("signing.secretKey").orNull
        if (secretKey != null && password != null) {
            useInMemoryPgpKeys(
                /* defaultKeyId = */ keyId,
                /* defaultSecretKey = */ secretKey,
                /* defaultPassword = */ password,
            )
        }
    }

    extensions.configure(MavenPublishBaseExtension::class.java) { publishing ->
        publishing.publishToMavenCentral()

        val dokkaJavadocJar = JavadocJar.Dokka(Constants.dokkaGeneratePublicationHtmlTaskName)
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