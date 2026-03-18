package org.hnau.plugins.project

import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val ProjectType.Kmp.commonMainSourceSet: NamedDomainObjectProvider<KotlinSourceSet>
    get() = kmpExtension
        .sourceSets
        .named("commonMain")

val ProjectType.Kmp.commonTestSourceSet: NamedDomainObjectProvider<KotlinSourceSet>
    get() = kmpExtension
        .sourceSets
        .named("commonTest")
