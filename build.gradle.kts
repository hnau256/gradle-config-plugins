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
    implementation(libs.google.services)
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


// Bump this together with `Versions.plugins.hnau` in Versions.kt
val versionString = "1.2.3"
val groupString = "org.hnau.gradle"
val artifactString = "plugins"

group = groupString
version = versionString

// Bump this together with Versions.plugins.hnau
gradlePlugin {
    plugins {
        val prefix = "org.hnau.plugin"
        
        create("HnauSettings") {
            id = "$prefix.settings"
            implementationClass = "org.hnau.plugins.settings.HnauSettingsPlugin"
            displayName = "Hnau Settings Plugin"
            description = "Centralized settings: version catalog, pluginManagement, auto-include modules, allModules defaults"
        }
        create("HnauJvm") {
            id = "$prefix.jvm"
            implementationClass = "org.hnau.plugins.project.entrypoints.HnauJvmPlugin"
            displayName = "Hnau JVM Plugin"
            description = "Kotlin JVM module configuration with auto-detection"
        }
        create("HnauKmp") {
            id = "$prefix.kmp"
            implementationClass = "org.hnau.plugins.project.entrypoints.HnauKmpPlugin"
            displayName = "Hnau KMP Plugin"
            description = "Kotlin Multiplatform module configuration with auto-detection"
        }
        create("HnauUi") {
            id = "$prefix.ui"
            implementationClass = "org.hnau.plugins.project.entrypoints.HnauUiPlugin"
            displayName = "Hnau UI Plugin"
            description = "Compose Multiplatform module configuration with auto-detection"
        }
        create("HnauAndroidApp") {
            id = "$prefix.androidapp"
            implementationClass = "org.hnau.plugins.project.entrypoints.HnauAndroidAppPlugin"
            displayName = "Hnau Android App Plugin"
            description = "Android Application module configuration with auto-detection"
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
        groupId = groupString,
        artifactId = artifactString,
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
