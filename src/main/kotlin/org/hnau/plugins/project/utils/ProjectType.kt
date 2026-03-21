package org.hnau.plugins.project.utils

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

sealed interface ProjectType {

    data object Jvm: ProjectType

    data class Kmp(
        val kmpExtension: KotlinMultiplatformExtension,
    ): ProjectType
}