import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    id("checkstyle")
    id("org.sonarqube") version "7.2.2.6593"
}

group = "com.b1"
version = "0.0.1-SNAPSHOT"
description = "MySawit"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

checkstyle {
    toolVersion = "10.12.5"
    isIgnoreFailures = false
    isShowViolations = true
}

sonar {
    properties {
        property("sonar.projectKey", "advprog-2026-B1-project_MySawit")
        property("sonar.organization", "advprog-2026-b1-project")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

configurations.named("checkstyle") {
    resolutionStrategy.capabilitiesResolution.withCapability("com.google.collections:google-collections") {
        select("com.google.guava:guava:0")
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named("sonar") {
    dependsOn(tasks.test)
}