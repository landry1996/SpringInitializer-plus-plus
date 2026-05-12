plugins {
    java
    id("org.springframework.boot") version "${springBootVersion}"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "${groupId}"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(${javaVersion})
    }
}

repositories {
    mavenCentral()
}

dependencies {
<#list dependencies as dep>
    implementation("${dep.groupId}:${dep.artifactId}")
</#list>
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
