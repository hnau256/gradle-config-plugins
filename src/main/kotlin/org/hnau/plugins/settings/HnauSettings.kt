package org.hnau.plugins.settings

/**
 * Container class for all hnau settings.
 * Stored in rootProject.extensions["hnauSettings"] for module plugins to access.
 */
open class HnauSettings {
    var includeCommonsKotlinDependency: Boolean = true
    var publishSettings: PublishSettings? = null
}
