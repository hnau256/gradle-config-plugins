package org.hnau.plugins.settings

/**
 * Data class to pass settings from settings plugin to module plugins.
 * Stored in rootProject.extensions["hnauSettingsData"]
 */
data class HnauSettingsData(
    val includeCommonsKotlinDependency: Boolean = true,
    val publishSettings: PublishSettings? = null,
)

/**
 * Publishing settings passed from settings.gradle.kts
 */
data class PublishSettings(
    val groupId: String,
    val gitUrl: String,
    val artifactId: String? = null,
    val version: String? = null,
    val description: String? = null,
    val developerName: String? = null,
    val developerEmail: String? = null,
    val licenseName: String? = null,
    val licenseUrl: String? = null,
)
