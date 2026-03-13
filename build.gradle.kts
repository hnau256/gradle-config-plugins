import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech)
    alias(libs.plugins.dokka)
    `java-gradle-plugin`
    signing
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
    implementation(libs.dokka)
    implementation(libs.vanniktech)

    // okhttp3 is a runtime dependency of vanniktech but needs to be on the plugin classpath
    // because vanniktech is applied programmatically (not via pluginManagement), so its runtime
    // transitive deps (okhttp-jvm, retrofit, etc.) don't get resolved into the plugin classloader
    implementation(libs.okhttp)
}

val jvmVersion = libs.versions.jvm
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
val versionString = "1.2.5"
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
            description =
                "Centralized settings: version catalog, pluginManagement, auto-include modules, allModules defaults"
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

extensions.configure<MavenPublishBaseExtension> {
    publishToMavenCentral()

    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
        ),
    )

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
                // defaultKeyId =
                keyId,
                // defaultSecretKey =
                secretKey,
                // defaultPassword =
                password,
            )
        }
    }

    coordinates(
        groupId = groupString,
        artifactId = artifactString,
        version = versionString,
    )

    val gitUrl = "https://github.com/hnau256/gradle-config-plugins"

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
