package org.hnau.plugins.settings

import org.gradle.api.Action
import org.hnau.plugins.utils.SharedConfig
import org.hnau.plugins.utils.requireNotNull
import org.hnau.plugins.utils.versions.GroupId

open class SharedConfigExtension {
    var groupId: String? = null

    var publish: SharedConfig.Publish? = null

    fun publish(action: Action<SharedConfigPublishBuilder>) {
        publish = SharedConfigPublishBuilder()
            .apply(action::execute)
            .build("${ConfigPath}publish")
    }

    internal val groupIdNotNull: GroupId
        get() = requireNotNull(
            value = groupId,
            propertyName = "${ConfigPath}groupId",
        ).let(::GroupId)

    companion object {

        private const val ConfigPath: String = "settings.gradle.kts/hnau/"
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

    fun build(
        configPath: String,
    ): SharedConfig.Publish = SharedConfig.Publish(
        gitUrl = requireNotNull(
            value = gitUrl,
            propertyName = "$configPath.gitUrl",
        ),
        version = requireNotNull(
            value = version,
            propertyName = "$configPath.version",
        ),
        description = description,
        developerName = developerName ?: "Mark Zorikhin",
        developerEmail = developerEmail ?: "hnau256@gmail.com",
        licenseName = licenseName ?: "MIT",
        licenseUrl = licenseUrl ?: "https://opensource.org/license/MIT",
    )
}
