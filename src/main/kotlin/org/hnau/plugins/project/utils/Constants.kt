package org.hnau.plugins.project.utils

internal object Constants {
    const val kspCommonMainKotlinMetadataTaskName = "kspCommonMainKotlinMetadata"
    const val dokkaGeneratePublicationHtmlTaskName = "dokkaGeneratePublicationHtml"
    const val copyHnauComposeStabilityConfigTaskName = "copyHnauComposeStabilityConfig"

    const val composeStabilityConfigResourcesFileName = "compose-stability.conf"

    const val composeStabilityConfigBuildFileName = "compose/stability.conf"

    const val desktopTargetName = "desktop"

    val kotlinFreeCompilerArgs: List<String> = listOf(
        "-opt-in=kotlin.time.ExperimentalTime",
    )
}