package org.hnau.plugins.settings

import org.gradle.api.Action
import org.hnau.plugins.utils.SharedConfig

open class SharedConfigExtension {
    var groupId: String? = null

    var publish: SharedConfig.Publish? = null

    fun publish(action: Action<SharedConfigPublishBuilder>) {
        publish =
            SharedConfigPublishBuilder()
                .apply(action::execute)
                .build()
    }
}

class SharedConfigPublishBuilder {
    var gitUrl: String? = null
    var version: String? = null
    var description: String? = null
    var developerName: String? = null
    var developerEmail: String? = null
    var licenseName: String? = null
    var licenseUrl: String? = null

    fun build(): SharedConfig.Publish =
        SharedConfig.Publish(
            gitUrl = gitUrl!!,
            version = version!!,
            description = description,
            developerName = developerName ?: "Mark Zorikhin",
            developerEmail = developerEmail ?: "hnau256@gmail.com",
            licenseName = licenseName ?: "MIT",
            licenseUrl = licenseUrl ?: "https://opensource.org/license/MIT",
        )
}
