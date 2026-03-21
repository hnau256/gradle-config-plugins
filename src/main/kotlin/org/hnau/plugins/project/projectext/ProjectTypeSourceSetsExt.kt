package org.hnau.plugins.project.projectext

import org.gradle.api.NamedDomainObjectProvider
import org.hnau.plugins.project.utils.ProjectType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal val ProjectType.Kmp.commonMainSourceSet: NamedDomainObjectProvider<KotlinSourceSet>
    get() = kmpExtension
        .sourceSets
        .named("commonMain")

internal val ProjectType.Kmp.commonTestSourceSet: NamedDomainObjectProvider<KotlinSourceSet>
    get() = kmpExtension
        .sourceSets
        .named("commonTest")
