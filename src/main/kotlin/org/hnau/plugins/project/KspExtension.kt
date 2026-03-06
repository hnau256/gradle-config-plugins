package org.hnau.plugins.project

/**
 * KSP configuration block.
 *
 * The KSP plugin is activated automatically when any processor flag is enabled.
 * Set [enabled] = true explicitly to use KSP with custom processors not covered by named flags.
 */
class KspExtension {
    var enabled: Boolean = false
    var pipe: Boolean = false
    var sealUp: Boolean = false
    var enumValues: Boolean = false
    var loggable: Boolean = false
    var arrowOptics: Boolean = false

    internal val isActive: Boolean
        get() = enabled || pipe || sealUp || enumValues || loggable || arrowOptics
}
