package org.hnau.plugins.settings

/**
 * Centralised defaults applied to every module via the project plugin.
 * Set these in the settings extension; the project plugin reads them at configuration time.
 */
class AllModulesExtension {
    var group: String? = null
    var version: String? = null
    var includeHnauCommons: Boolean = true
}
