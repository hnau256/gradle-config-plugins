package org.hnau.plugins.utils.versions

enum class ComposeDependencyType {
    Runtime,
    Foundation,
    FoundationLayout,
    Ui,
    Material3,
    IconsCore,
    IconsExtended,
}

data class ComposeDependencyTypeValues<out T>(
    val runtime: T,
    val foundation: T,
    val foundationLayout: T,
    val ui: T,
    val material3: T,
    val iconsCore: T,
    val iconsExtended: T,
) {

    operator fun get(
        type: ComposeDependencyType,
    ): T = when (type) {
        ComposeDependencyType.Runtime -> runtime
        ComposeDependencyType.Foundation -> foundation
        ComposeDependencyType.FoundationLayout -> foundationLayout
        ComposeDependencyType.Ui -> ui
        ComposeDependencyType.Material3 -> material3
        ComposeDependencyType.IconsCore -> iconsCore
        ComposeDependencyType.IconsExtended -> iconsExtended
    }
}

