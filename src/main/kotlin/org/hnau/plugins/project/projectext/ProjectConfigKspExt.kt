package org.hnau.plugins.project.projectext

import org.gradle.api.Project
import org.gradle.api.tasks.AbstractCopyTask
import org.hnau.plugins.Versions
import org.hnau.plugins.project.utils.Constants
import org.hnau.plugins.project.utils.ProjectType
import org.hnau.plugins.utils.versions.LibraryId
import org.hnau.plugins.utils.versions.Versioned
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.configureKsp(
    projectType: ProjectType,
) {
    fun addProcessor(
        dependency: Versioned<LibraryId>,
    ) {
        addDependency(
            configurationName = when (projectType) {
                ProjectType.Jvm -> "ksp"
                is ProjectType.Kmp -> "kspCommonMainMetadata"
            },
            dependency = dependency,
        )
    }

    Versions.HnauCommons.gen.forEach { annotationWithProcessor ->
        addDependency(
            type = projectType,
            dependency = annotationWithProcessor.annotation,
        )
        addProcessor(annotationWithProcessor.processor)
    }

    addProcessor(Versions.Arrow.opticsProcessor)

    when (projectType) {
        ProjectType.Jvm -> Unit
        is ProjectType.Kmp -> {
            projectType.commonMainSourceSet.configure { sourceSet: KotlinSourceSet ->
                sourceSet.kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            }

            tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->
                if (task.name == Constants.kspCommonMainKotlinMetadataTaskName) {
                    return@configureEach
                }
                task.dependsOn(Constants.kspCommonMainKotlinMetadataTaskName)
            }
            tasks.withType(AbstractCopyTask::class.java).configureEach { task ->
                task.dependsOn(Constants.kspCommonMainKotlinMetadataTaskName)
            }
        }
    }
}