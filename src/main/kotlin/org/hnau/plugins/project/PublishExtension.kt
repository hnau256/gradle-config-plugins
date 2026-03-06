package org.hnau.plugins.project

class PublishExtension {
    /** Git repository URL, e.g. "https://github.com/hnau256/mylib". Required. */
    var gitUrl: String? = null

    /** Short description of the module. Defaults to module name. */
    var description: String? = null

    /** SPDX license identifier, e.g. "MIT", "Apache-2.0". Defaults to "MIT". */
    var license: String = "MIT"

    /** Maven groupId override. Defaults to allModules.group or project.group. */
    var group: String? = null
}
