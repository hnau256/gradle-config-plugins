import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech)
    alias(libs.plugins.dokka)
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    implementation(libs.kgp)
    implementation(libs.kgp.serialization)
    implementation(libs.kgp.compose)
    implementation(libs.agp)
    implementation(libs.agp.api)
    implementation(libs.ksp)
    implementation(libs.compose)
    implementation(libs.vanniktech)
    implementation(libs.dokka)
}

val jvmVersion =
    libs.versions.jvm
        .get()
        .toInt()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
}

kotlin {
    compilerOptions {
        jvmTarget =
            org.jetbrains.kotlin.gradle.dsl.JvmTarget
                .fromTarget(libs.versions.jvm.get())
    }
}

val versionString = "1.0.0"

group = "org.hnau.plugins"
version = versionString

gradlePlugin {
    plugins {
        create("hnauSettings") {
            id = "org.hnau.settings"
            implementationClass = "org.hnau.plugins.settings.HnauSettingsPlugin"
            displayName = "Hnau Settings Plugin"
            description = "Centralized settings: version catalog, pluginManagement, auto-include modules, allModules defaults"
        }
        create("hnauProject") {
            id = "org.hnau.project"
            implementationClass = "org.hnau.plugins.project.HnauProjectPlugin"
            displayName = "Hnau Project Plugin"
            description = "Module configuration: JVM/KMP setup, KSP processors, serialization, publishing"
        }
    }
}

val gitUrl = "https://github.com/hnau256/gradle-config-plugins"

extensions.configure<MavenPublishBaseExtension> {
    publishToMavenCentral()

    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
        ),
    )

    coordinates(
        groupId = "org.hnau.plugins",
        artifactId = "plugins",
        version = versionString,
    )

    pom {
        name.set("Hnau Gradle Plugins")
        description.set("Settings and project convention plugins for hnau projects")
        url.set(gitUrl)
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("hnau256")
                name.set("Mark Zorikhin")
                email.set("hnau256@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:$gitUrl.git")
            url.set(gitUrl)
        }
    }
}
