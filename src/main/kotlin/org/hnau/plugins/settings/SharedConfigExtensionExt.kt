package org.hnau.plugins.settings

import org.hnau.plugins.utils.SharedConfig

fun SharedConfigExtension.toSharedConfig(): SharedConfig = SharedConfig(
    groupId = groupIdNotNull,
    publish = publish,
)