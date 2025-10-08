rootProject.name = "message-hub"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        kotlin("kapt") version kotlinVersion
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":common",
    ":domain",
    ":api",
    ":worker",
    ":provider",
    ":external:client",
    ":external:storage",
)