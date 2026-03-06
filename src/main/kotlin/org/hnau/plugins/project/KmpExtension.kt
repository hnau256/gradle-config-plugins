package org.hnau.plugins.project

class KmpExtension {
    var compose: Boolean = false

    /**
     * Adds org.hnau.commons:app-model dependency.
     * If compose==true, also adds org.hnau.commons:app-projector.
     */
    var app: Boolean = false
}
