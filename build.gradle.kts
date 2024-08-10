import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI


plugins {
    id("stickfix.conventions")
}

group = "cl.ravenhill"
version = libs.versions.stickfix

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/r8vnhill/strait-jakt")
        credentials {
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
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
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.date.time)
    implementation(libs.arrow.core)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.framework.datatest)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.property.arbs)
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