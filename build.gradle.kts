import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

val dokkaVersion = extra["dokka.version"] as String
val jaktVersion = extra["jakt.version"] as String
val kotestVersion = extra["kotest.version"] as String
val kotestExtensionVersion = extra["kotest.extension.version"] as String
val kotlinxDatetimeVersion = extra["kotlinx.datetime.version"] as String
val exposedVersion = extra["exposed.version"] as String
val h2Version = extra["h2.version"] as String
val logbackVersion = extra["logback.version"] as String
val kotlinTelegramBotVersion = extra["kotlin.telegram.bot.version"] as String
val systemLambdaVersion = extra["system-lambda.version"] as String

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    kotlin("jvm")
}

group = "cl.ravenhill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("cl.ravenhill:strait-jakt:$jaktVersion")
    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:dispatcher:$kotlinTelegramBotVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
    testImplementation("io.kotest.extensions:kotest-property-arbs:$kotestExtensionVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("com.github.stefanbirkner:system-lambda:$systemLambdaVersion")
}

tasks.test {
    useJUnitPlatform()
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
    // Configures the JVM toolchain to use version 8 of the JDK
    jvmToolchain(21)
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
            compilerOptions {
                freeCompilerArgs = listOf("-Xcontext-receivers")
            }
        }
    }
}
