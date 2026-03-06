package org.hnau.plugins.project

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.PluginIds
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class HnauProjectPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.extensions.findByType(HnauProjectExtension::class.java) != null) return

        project.plugins.apply(PluginIds.dokka)
        project.plugins.apply(PluginIds.vanniktech)
        project.plugins.apply(PluginIds.signing)

        project.extensions.create("hnau", HnauProjectExtension::class.java, project)

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
}
