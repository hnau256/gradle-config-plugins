package org.hnau.plugins.project

import org.gradle.api.Project
import org.hnau.plugins.ConfigurationNames
import org.hnau.plugins.TaskNames
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.HnauCommons
import org.hnau.plugins.Versions.PluginIds
import org.hnau.plugins.project.extensions.KspExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal class KspConfigurator(
    private val project: Project,
) {
    fun configure(
        kspExtension: KspExtension,
        moduleType: ModuleType,
    ) {
        if (!project.plugins.hasPlugin(PluginIds.ksp)) {
            project.plugins.apply(PluginIds.ksp)
        }
        applyKspDeps(moduleType, kspExtension)
    }

    private fun applyKspDeps(
        currentModule: ModuleType,
        kspExtension: KspExtension,
    ) {
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
            if (kspExtension.pipe) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.pipeAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.pipeProcessor),
                )
            }
            if (kspExtension.sealUp) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.sealUpAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.sealUpProcessor),
                )
            }
            if (kspExtension.enumValues) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.enumValuesAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.enumValuesProcessor),
                )
            }
            if (kspExtension.loggable) {
                kspProcessor(
                    HnauCommons.dep(HnauCommons.Gen.loggableAnnotations),
                    HnauCommons.dep(HnauCommons.Gen.loggableProcessor),
                )
            }
        }
        if (kspExtension.arrowOptics) {
            kspAllTargets(Versions.Arrow.opticsKspPlugin())
        }

        if (currentModule is ModuleType.Kmp) {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlinExtension.sourceSets.getByName("commonMain").kotlin.srcDir(
                project.layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"),
            )

            project.tasks.configureEach { task ->
                if (task is KotlinCompilationTask<*> ||
                    task is org.gradle.api.tasks.bundling.Jar ||
                    task.name.contains("sourcesJar", ignoreCase = true)
                ) {
                    if (task.name != TaskNames.kspCommonMainKotlinMetadata) {
                        task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
                    }
                }
            }

            project.tasks
                .matching { task -> task.name.startsWith("ksp") && task.name != TaskNames.kspCommonMainKotlinMetadata }
                .configureEach { task ->
                    task.dependsOn(TaskNames.kspCommonMainKotlinMetadata)
                }
        }
    }
}
