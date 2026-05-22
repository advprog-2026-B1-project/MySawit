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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("net.serenity-bdd:serenity-junit5:4.1.14")
    testImplementation("net.serenity-bdd:serenity-spring:4.1.14")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    runtimeOnly("com.h2database:h2")
    testImplementation("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testRuntimeOnly("com.h2database:h2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    exclude("**/functional/**")
    finalizedBy(tasks.named("jacocoTestReport"))
    systemProperty("serenity.outputDirectory", "${layout.buildDirectory.get()}/site/serenity")
}

tasks.register<Test>("functionalTest") {
    useJUnitPlatform()
    include("**/functional/**")
    systemProperty("serenity.outputDirectory", "${layout.buildDirectory.get()}/site/serenity")
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
}

tasks.named("sonar") {
    dependsOn(tasks.test)
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val envFile = file("../.env")
    if (envFile.exists()) {
        envFile.readLines().forEach {
            if (it.isNotBlank() && !it.startsWith("#")) {
                val split = it.split("=", limit = 2)
                if (split.size == 2) {
                    environment(split[0].trim(), split[1].trim().removeSurrounding("\""))
                }
            }
        }
    }
}