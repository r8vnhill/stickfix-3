rootProject.name = "Stickfix3"

pluginManagement {
    val jvm = "jvm"
    val detekt = "detekt"
    val dokka = "dokka"

    @Suppress("SpellCheckingInspection")
    val arturBosch = "io.gitlab.arturbosch"
    val jetBrains = "org.jetbrains"
    val kotlin = "kotlin"

    /**
     * Configures plugin dependencies for the root project Stickfix3 using a custom method
     * to resolve plugin versions from the project's extra properties.
     */
    fun resolvePluginDependencyVersion(
        dependency: String,
        versionKey: String,
        constructor: (String) -> PluginDependencySpec
    ) = constructor(dependency) version extra["$versionKey.version"] as String

    repositories {
        gradlePluginPortal()
    }

    plugins {
        resolvePluginDependencyVersion(jvm, kotlin, ::kotlin)
        resolvePluginDependencyVersion("$arturBosch.$detekt", detekt, ::id)
        resolvePluginDependencyVersion("$jetBrains.$dokka", dokka, ::id)
    }
}