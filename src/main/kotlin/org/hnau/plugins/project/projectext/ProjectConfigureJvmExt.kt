package org.hnau.plugins.project.projectext

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.Constants
import org.hnau.plugins.project.utils.ProjectConfig
import org.hnau.plugins.project.utils.ProjectType
import org.hnau.plugins.project.utils.androidNamespace
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureJvm(
    config: ProjectConfig,
    addAndroid: Boolean,
): ProjectType {

    val projectType = ProjectType.Jvm

    when (addAndroid) {
        false -> {
            applyPlugin(Versions.Plugins.kotlinJvm.withoutAlias.withoutVersion)

            project
                .extensions
                .getByType(KotlinJvmProjectExtension::class.java)
                .jvmToolchain(Versions.jvmTargetInt)

            project
                .extensions
                .getByType(JavaPluginExtension::class.java)
                .targetCompatibility = JavaVersion.toVersion(Versions.jvmTargetInt)

            tasks.withType(JavaCompile::class.java).configureEach { task ->
                task.options.release.set(Versions.jvmTargetInt)
            }
        }

        true -> {
            applyPlugin(Versions.Plugins.googleServices.withoutAlias.withoutVersion)
            applyPlugin(Versions.Plugins.androidApplication.withoutAlias.withoutVersion)
            applyPlugin(Versions.Plugins.kotlinAndroid.withoutAlias.withoutVersion)
            applyKotlinComposePlugin()

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

            addAndroidDependencies(
                projectType = projectType,
                addCompose = true,
            )

            addComposeDependencies(
                dependencies = Versions.jetpackCompose,
                projectType = projectType,
            )
        }
    }

    tasks.withType(KotlinCompile::class.java).configureEach { task ->
        task.compilerOptions {
            jvmTarget.set(Versions.jvmTarget)
            freeCompilerArgs.addAll(Constants.kotlinFreeCompilerArgs)
        }
    }

    return projectType
}