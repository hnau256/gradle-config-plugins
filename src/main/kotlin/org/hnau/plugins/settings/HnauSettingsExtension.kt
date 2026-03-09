package org.hnau.plugins.settings

import org.gradle.api.Action
import org.gradle.api.initialization.Settings

/**
 * Extension for settings.gradle.kts to configure hnau plugins.
 *
 * Example usage:
 * ```kotlin
 * hnau {
 *     includeCommonsKotlinDependency = true
 *     publish {
 *         groupId = "hnau.pinfin"
 *         gitUrl = "https://github.com/hnau/pinfin"
 *     }
 * }
 * ```
 */
open class HnauSettingsExtension(
    private val settings: Settings,
) {
    var includeCommonsKotlinDependency: Boolean = true

    internal var publishSettings: PublishSettings? = null

    /**
     * Configure publishing for all modules.
     * If this block is present, all modules with hnau plugins will be configured for publishing.
     */
    fun publish(action: Action<PublishSettingsBuilder>) {
        val builder = PublishSettingsBuilder()
        action.execute(builder)
        publishSettings = builder.build()
    }
}

/**
 * Builder for PublishSettings with required fields validation.
 */
class PublishSettingsBuilder {
    var groupId: String? = null
    var gitUrl: String? = null
    var artifactId: String? = null
    var version: String? = null
    var description: String? = null
    var developerName: String? = null
    var developerEmail: String? = null
    var licenseName: String? = null
    var licenseUrl: String? = null

    fun build(): PublishSettings {
        requireNotNull(groupId) { "groupId is required in hnau.publish {}" }
        requireNotNull(gitUrl) { "gitUrl is required in hnau.publish {}" }

        return PublishSettings(
            groupId = groupId!!,
            gitUrl = gitUrl!!,
            artifactId = artifactId,
            version = version,
            description = description,
            developerName = developerName,
            developerEmail = developerEmail,
            licenseName = licenseName,
            licenseUrl = licenseUrl,
        )
    }
}
