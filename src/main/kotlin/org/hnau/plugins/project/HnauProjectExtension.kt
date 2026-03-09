package org.hnau.plugins.project

import org.gradle.api.Action
import org.gradle.api.Project
import org.hnau.plugins.ConfigurationNames
import org.hnau.plugins.Versions
import org.hnau.plugins.Versions.PluginIds
import org.hnau.plugins.project.extensions.KmpExtension
import org.hnau.plugins.project.extensions.KspExtension
import org.hnau.plugins.project.extensions.PublishExtension

open class HnauProjectExtension(
    private val project: Project,
) {
    val publish: PublishExtension = PublishExtension()
    val ksp: KspExtension = KspExtension()

    private val publishingConfigurator = PublishingConfigurator(project, publish)
    private val moduleConfigurator = ModuleConfigurator(project, publishingConfigurator)
    private val kspConfigurator = KspConfigurator(project)

    var serialization: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (!project.plugins.hasPlugin(PluginIds.kotlinSerialization)) {
                    project.plugins.apply(PluginIds.kotlinSerialization)
                }
                addDep(Versions.Kotlinx.serializationCore())
                addDep(Versions.Kotlinx.serializationJson())
            }
        }

    fun publish(action: Action<PublishExtension>) {
        action.execute(publish)
    }

    fun jvm(includeHnauCommons: Boolean? = null) {
        moduleConfigurator.jvm(includeHnauCommons)
    }

    fun kmp(
        includeHnauCommons: Boolean? = null,
        action: Action<KmpExtension> = Action {},
    ) {
        val kmpExtension = KmpExtension()
        action.execute(kmpExtension)
        moduleConfigurator.kmp(includeHnauCommons, kmpExtension)
    }

    fun ksp(action: Action<KspExtension>) {
        action.execute(ksp)
        val currentModule =
            checkNotNull(moduleConfigurator.getModuleType()) {
                "ksp() must be called after jvm() or kmp() in module '${project.path}'"
            }
        if (ksp.isActive) {
            kspConfigurator.configure(ksp, currentModule)
        }
    }

    private fun addDep(notation: String) {
        val configuration =
            if (project.plugins.hasPlugin(PluginIds.kotlinMultiplatform)) {
                ConfigurationNames.commonMainImplementation
            } else {
                ConfigurationNames.implementation
            }
        project.dependencies.add(configuration, notation)
    }
}
