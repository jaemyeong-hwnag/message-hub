val projectGroup = providers.gradleProperty("group").get()
val applicationVersion = providers.gradleProperty("version").get()
val jvmTarget = providers.gradleProperty("jvmTarget").get().toInt()

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    kotlin("kapt")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmTarget)
    }
}

allprojects {
    group = projectGroup
    version = applicationVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        // integration 태그가 있는 테스트는 기본 test 태스크에서 제외
        useJUnitPlatform {
            excludeTags("integration")
        }
    }

    // integration 테스트를 위한 별도 태스크
    tasks.register<Test>("integrationTest") {
        group = "verification"
        description = "Runs integration tests"
        useJUnitPlatform {
            includeTags("integration")
        }
        shouldRunAfter("test")
    }
}

// Spring Boot 애플리케이션 모듈들에만 Spring Boot 플러그인 적용
configure(subprojects.filter { it.name in listOf("api") }) {
    apply(plugin = "org.springframework.boot")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}