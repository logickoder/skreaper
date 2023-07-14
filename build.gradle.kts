import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlin = "1.9.0"
    kotlin("jvm") version kotlin
    kotlin("plugin.serialization") version kotlin
    application
}

group = "dev.logickoder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.0"
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.htmlunit:htmlunit:3.3.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

application {
    mainClass.set("dev.logickoder.skreaper.ApplicationKt")
}