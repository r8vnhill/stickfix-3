import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI


plugins {
    id("stickfix.conventions")
}

group = "cl.ravenhill"
version = libs.versions.stickfix

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.jakt)
    implementation(libs.kotlin.`as`.java)
    implementation(libs.logback)
    implementation(libs.kotlin.telegram.bot)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.kotlinx.date.time)
    implementation(libs.kotest.assertions.core)
    implementation(libs.kotest.framework.engine)
    implementation(libs.kotest.framework.datatest)
    implementation(libs.kotest.property)
    implementation(libs.kotest.runner.junit5)
    implementation(libs.kotest.property.arbs)
}

tasks.withType<DokkaTask>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
}

val dokkaHtml by tasks.getting(DokkaTask::class)

val dokkaJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

kotlin {
    sourceSets.all {
        languageSettings {
            compilerOptions {
                freeCompilerArgs = listOf("-Xcontext-receivers")
            }
        }
    }
}

tasks.jar {
    archiveBaseName.set("Stickfix3")
    archiveVersion.set(libs.versions.stickfix.get())
    manifest {
        attributes(
            "Main-Class" to "cl.ravenhill.stickfix.MainKt"
        )
    }
    from(sourceSets.main.get().output)
}