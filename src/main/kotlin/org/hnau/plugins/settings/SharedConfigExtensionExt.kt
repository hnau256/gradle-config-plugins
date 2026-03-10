package org.hnau.plugins.settings

import org.hnau.plugins.utils.SharedConfig
import org.hnau.plugins.utils.versions.GroupId

fun SharedConfigExtension.toSharedConfig(): SharedConfig = SharedConfig(
    groupId = groupId!!.let(::GroupId),
    publish = publish,
)