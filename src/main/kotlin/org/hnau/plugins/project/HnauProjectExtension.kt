package org.hnau.plugins.project

import org.gradle.api.Action

open class HnauProjectExtension {
    /**
     * Module type. Must be set exactly once.
     * Use [jvm] or [kmp] factory functions.
     */
    var module: ModuleType = ModuleType.Jvm

    fun jvm(): ModuleType.Jvm = ModuleType.Jvm.also { module = it }

    fun kmp(action: Action<ModuleType.Kmp>): ModuleType.Kmp {
        val kmp = ModuleType.Kmp()
        action.execute(kmp)
        module = kmp
        return kmp
    }

    /**
     * Whether to include org.hnau.commons:kotlin in this module.
     * Inherits the value from settings allModules.includeHnauCommons by default.
     * Override per-module here if needed.
     */
    var includeHnauCommons: Boolean? = null

    var serialization: Boolean = false

    val publish: PublishExtension = PublishExtension()

    fun publish(action: Action<PublishExtension>) {
        action.execute(publish)
    }

    val ksp: KspExtension = KspExtension()

    fun ksp(action: Action<KspExtension>) {
        action.execute(ksp)
    }
}
