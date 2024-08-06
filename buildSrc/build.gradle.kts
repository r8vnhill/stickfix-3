/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

plugins {
   `kotlin-dsl`
}

dependencies {
   implementation(libs.kotlin.gradle.plugin)
   implementation(libs.dokka)
   implementation(libs.detekt)
}
