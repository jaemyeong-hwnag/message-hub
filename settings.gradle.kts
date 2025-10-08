rootProject.name = "message-hub"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // gradle.properties 값 읽기
    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val springBootVersion = providers.gradleProperty("springBootVersion").get()
    val springDepMgmtVersion = providers.gradleProperty("springDependencyManagementVersion").get()

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDepMgmtVersion
    }
}