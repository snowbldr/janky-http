plugins {
    kotlin("jvm") version "1.6.10"
    id("com.diffplug.spotless") version "6.2.2"
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

publishing {
    publications {
        create<MavenPublication>("jankyHttp") {
            from(components["kotlin"])
        }
    }
}

spotless {
    kotlin {
        ktlint()
    }
}