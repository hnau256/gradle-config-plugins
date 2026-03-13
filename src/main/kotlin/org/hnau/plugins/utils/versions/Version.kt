package org.hnau.plugins.utils.versions

enum class Version(
    val version: String,
) {
    Kotlin("2.3.10"),
    AndroidGradlePlugin("9.1.0"),
    ComposeMultiplatform("1.10.2"),
    ComposeMultiplatformMaterial3("1.10.0-alpha05"),
    HnauPlugins("1.2.5"),
    HnauCommons("1.3.0"),
    KotlinxSerialization("1.10.0"),
    CommposeMultiplatformIcons("1.7.3"),
    JetpackCompose("1.7.6"),
    JetpackComposeMaterial3("1.3.1"),
    ActivityCompose("1.9.3"),

    AndroidAppCompat("1.7.1"),
    LifecycleViewmodelCompose("2.8.7"),
    GoogleServicesPlugin("4.4.4"),
    Ksp("2.3.6"),
    DokkaPlugin("2.1.0"),
    VanniktechPlugin("0.36.0"),
    KotlinImmutable("0.4.0"),
    Arrow("2.2.2"),

    KotlinxCoroutines("1.10.2"),
    KotlinxDateTime("0.7.1"),
    KotlinxAtomicFu("0.31.0"),
    KotlinxIO("0.9.0"),
    Kermit("2.0.8"),
    Kotlinpoet("2.2.0");

    val alias: Alias = name
        .replaceFirstChar(Char::lowercase)
        .let(::Alias)
}