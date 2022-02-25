plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
}

group = "com.github.snowbldr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
}
