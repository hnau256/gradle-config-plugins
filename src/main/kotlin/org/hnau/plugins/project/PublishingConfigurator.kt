package org.hnau.plugins.project

import com.vanniktech.maven.publish.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.hnau.plugins.project.extensions.PublishExtension

internal class PublishingConfigurator(
    private val project: Project,
    private val publishExtension: PublishExtension,
) {
    fun configure() {
        val artifactId = project.path.removePrefix(":").replace(":", "-")
        val groupId =
            publishExtension.group
                ?: project.extensions.extraProperties.properties["hnau.group"] as? String
                ?: throw GradleException(
                    "allModules.group is required. Set it via allModules { group = \"com.example\" } in settings.",
                )
        val gitUrl =
            publishExtension.gitUrl
                ?: project.extensions.extraProperties.properties["hnau.gitUrl"] as? String
                ?: throw GradleException(
                    "gitUrl is required. Set it via allModules { gitUrl = \"...\" } in settings or publish { gitUrl = \"...\" } in the module.",
                )
        val description = publishExtension.description ?: project.name
        val licenseId = publishExtension.license
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

    private fun spdxLicenseUrl(spdxId: String) =
        when (spdxId) {
            "MIT" -> "https://opensource.org/licenses/MIT"
            "Apache-2.0" -> "https://www.apache.org/licenses/LICENSE-2.0"
            "GPL-3.0" -> "https://www.gnu.org/licenses/gpl-3.0.html"
            "LGPL-2.1" -> "https://www.gnu.org/licenses/lgpl-2.1.html"
            else -> "https://opensource.org/licenses/$spdxId"
        }
}
