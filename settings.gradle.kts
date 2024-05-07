rootProject.name = "Stickfix3"

pluginManagement {
    val jvm = "jvm"
    val detekt = "detekt"
    val dokka = "dokka"

    @Suppress("SpellCheckingInspection")
    val arturBosch = "io.gitlab.arturbosch"
    val jetBrains = "org.jetbrains"
    val kotlinVersionKey = "kotlin.version"
    val detektVersionKey = "detekt.version"
    val dokkaVersionKey = "dokka.version"

    /**
     * Resolves and applies a plugin dependency version using a specified key from project properties.
     *
     * @param dependency The plugin's ID.
     * @param versionKey The key to retrieve the version from the project's extra properties.
     * @param constructor A lambda that constructs a PluginDependencySpec with the plugin ID.
     * @return A PluginDependencySpec with the applied version.
     * @throws IllegalArgumentException if the version key is not found in project properties.
     */
    fun resolvePluginDependencyVersion(
        dependency: String,
        versionKey: String,
        constructor: (String) -> PluginDependencySpec
    ): PluginDependencySpec {
        val version = extra[versionKey] as? String
            ?: throw IllegalArgumentException("Version key '$versionKey' not found.")
        return constructor(dependency) version version
    }

    repositories {
        gradlePluginPortal()
    }

    plugins {
        resolvePluginDependencyVersion(jvm, kotlinVersionKey, ::kotlin)
        resolvePluginDependencyVersion("$arturBosch.$detekt", detektVersionKey, ::id)
        resolvePluginDependencyVersion("$jetBrains.$dokka", dokkaVersionKey, ::id)
    }
}
